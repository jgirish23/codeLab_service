package com.codelab.codelab.controller;

import com.pty4j.PtyProcess;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class TerminalController {

    private PtyProcess ptyProcess;
    private InputStream processInputStream;
    private OutputStream processOutputStream;
    private final Object lock = new Object(); // Lock for synchronization
    private Thread t1;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public TerminalController() throws IOException {
        initializeTerminalProcess();
    }

    @PostConstruct
    private void startReadingThread() {
        t1 = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (lock) {
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = processInputStream.read(buffer)) != -1) {
                            String output = new String(buffer, 0, bytesRead);
                            log.info("Terminal Output: " + output);
                            messagingTemplate.convertAndSend("/queue/reply", output);
                        }
                    } catch (IOException e) {
                        log.error("Error reading process output:", e);
                        reconnectTerminalProcess();
                    }
                }
            }
        });
        t1.start();


    }

    private void initializeTerminalProcess() throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("TERM", "xterm-256color");
        ptyProcess = PtyProcess.exec(
                new String[]{"/bin/bash", "-i", "-c", "stty raw -echo; trap 'exit' INT; exec bash"},
                env,
                null
        );
        processInputStream = ptyProcess.getInputStream();
        processOutputStream = ptyProcess.getOutputStream();
    }

    private void reconnectTerminalProcess() {
        try {
            // Clean up existing resources
            if (ptyProcess != null) {
                ptyProcess.destroy();
            }
            if (processInputStream != null) {
                processInputStream.close();
            }
            if (processOutputStream != null) {
                processOutputStream.close();
            }

            // Reinitialize the terminal process
            log.info("Terminal process reconnected.");
            initializeTerminalProcess();
        } catch (IOException e) {
            log.error("Failed to reconnect terminal process:", e);
        }
    }

    @MessageMapping("/execute")
    public void handleCommand(String command) throws IOException {
        try {
            log.info("Command received: " + command);
            if (command.equals("\u0003")) { // Handle Ctrl+C
                log.info("Sending SIGINT (Ctrl+C) to the process.");
//                processOutputStream.write((command + "\n").getBytes()); // Send other commands
//                processOutputStream.flush();
//                t1.interrupt();
                reconnectTerminalProcess(); // Restart shell if needed
            } else {
                processOutputStream.write((command + "\n").getBytes()); // Send other commands
                processOutputStream.flush();
            }
        } catch (IOException e) {
            log.error("Error writing command to process: ", e);
        }
    }

    @MessageMapping("/runFile")
    public void runFileHandleCommand(String command) throws IOException {
        try {
            log.info("Command received: " + command);
            JSONParser jsonParser = new JSONParser(command);
            Map<String, Object> jsonObject = jsonParser.parseObject();

            String filePath = jsonObject.get("path").toString();
            String runCommand = jsonObject.get("command").toString();
            String projectType = jsonObject.get("projectType").toString();
            String fileUploadpath = System.getProperty("user.dir");
            String fullFilePath = fileUploadpath+File.separator + filePath;
            if(projectType.equals("React js")){
                processOutputStream.write((runCommand + "\n").getBytes()); // Send other commands
            }else{
                processOutputStream.write((runCommand + " " +fullFilePath + "\n").getBytes()); // Send other commands
            }
            processOutputStream.flush();
        } catch (Exception e) {
            log.error("Error writing command to process: ", e);
        }
    }

    @PreDestroy
    public void destroy() {
        synchronized (lock) {
            try {
                if (ptyProcess != null) {
                    ptyProcess.destroy();
                }
                if (processInputStream != null) {
                    processInputStream.close();
                }
                if (processOutputStream != null) {
                    processOutputStream.close();
                }
            } catch (IOException e) {
                log.error("Error cleaning up resources:", e);
            }
        }
    }
}
