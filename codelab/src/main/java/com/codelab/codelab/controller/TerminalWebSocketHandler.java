package com.codelab.codelab.controller;

import com.pty4j.PtyProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private PtyProcess pty;
    private InputStream in;
    private OutputStream out;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        startShell();
        startReader(session);
        log.info("✅ Terminal connected");
    }

    private void startShell() throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("TERM", "xterm-256color");
        env.put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");

        pty = PtyProcess.exec(
                new String[]{"/bin/bash", "--login"},
                env,
                System.getProperty("user.dir")
        );

        in = pty.getInputStream();
        out = pty.getOutputStream();
    }

    private void startReader(WebSocketSession session) {
        new Thread(() -> {
            byte[] buffer = new byte[4096];
            try {
                int n;
                while ((n = in.read(buffer)) > 0) {
                    session.sendMessage(
                            new TextMessage(new String(buffer, 0, n))
                    );
                }
            } catch (Exception e) {
                log.error("PTY read error", e);
            }
        }, "pty-reader").start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {

        String cmd = message.getPayload();

        if ("__ping__".equals(cmd)) {
            return; // just keep alive
        }

        // IMPORTANT: Postman does NOT send Enter key
        if (!cmd.endsWith("\n")) {
            cmd += "\n";
        }

        out.write(cmd.getBytes());
        out.flush();
    }

    @PostMapping("/runFile")
    public ResponseEntity<String> runFileHandleCommand(@RequestBody String command) {
        try {
            log.info("Command received: {}", command);

            JSONParser jsonParser = new JSONParser(command);
            Map<String, Object> jsonObject = jsonParser.parseObject();

            String filePath = jsonObject.get("path").toString();
            String runCommand = jsonObject.get("command").toString();
            String projectType = jsonObject.get("projectType").toString();

            String basePath = System.getProperty("user.dir");
            String fullFilePath = basePath + File.separator + filePath;

            String finalCommand;

            if ("React js".equals(projectType)) {
                finalCommand = runCommand;
            } else {
                finalCommand = runCommand + " " + fullFilePath;
            }

            out.write((finalCommand + "\n").getBytes());
            out.flush();

            return ResponseEntity.ok("File executed!");
        } catch (Exception e) {
            log.error("Error writing command to process", e);
            return ResponseEntity.internalServerError().body("File execution failed");
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            if (pty != null) pty.destroy();
        } catch (Exception ignored) {}
        log.info("❌ Terminal closed");
    }
}
