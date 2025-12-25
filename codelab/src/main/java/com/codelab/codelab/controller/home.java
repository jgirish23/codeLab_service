package com.codelab.codelab.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
@RestController
public class home {

    private static Map<String, Object> listFiles(File directory) {
        Map<String, Object> dirMap = new LinkedHashMap<>();
        List<String> filesList = new ArrayList<>();
        List<String> subDirs = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    subDirs.add(file.getName()); // Recursive call for subdirectory
                } else {
                    filesList.add(file.getName()); // Add file name to list
                }
            }
        }

        for(String file : subDirs){
            dirMap.put(file, "Directory");
        }
        for(String file : filesList){
            dirMap.put(file, null);
        }
        return dirMap;
    }

    @GetMapping("/files")
    public String getFileTree(@RequestParam("path") String path) throws JsonProcessingException {
        log.info("File directory to json");

        // Get the current directory
        File currentDir = new File(System.getProperty("user.dir"));

        File dirPath = new File(currentDir + File.separator + path);
        String DirName = Paths.get(dirPath.toString()).getFileName().toString();

        // Get directory structure
        Map<String, Object> directoryTree = new HashMap<>();
        directoryTree.put(DirName, listFiles(dirPath));

        // Convert to JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(directoryTree);

        return json;
    }

    @PostMapping("/file")
    public ResponseEntity<?> saveFile(@RequestBody String fileContent, @RequestParam("path") String path) throws IOException {
        String fileUploadpath = System.getProperty("user.dir");

        // Setting up the filepath
        String filePath = fileUploadpath+File.separator+path;
        Path uploadPath = Path.of(filePath);

        Files.write(uploadPath, fileContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return ResponseEntity.ok("Filed saved Successfully");
    }

    @GetMapping("/file")
    public ResponseEntity<?> getFile(@RequestParam("path") String path) throws JsonProcessingException, FileNotFoundException {
        String fileUploadpath = System.getProperty("user.dir");

        // Setting up the filepath
        String filePath = fileUploadpath+File.separator+path;

        // Creating new file instance
        File file= new File(filePath);

        // Creating a new InputStreamResource object
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        // Creating a new instance of HttpHeaders Object
        HttpHeaders headers = new HttpHeaders();

        // Setting up values for contentType and headerValue
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    private String[] getFiles()
    {
        String folderPath = System.getProperty("user.dir");

        // Creating a new File instance
        File directory= new File(folderPath);

        String[] filenames = directory.list();

        // returning the list of filenames
        return filenames;

    }
}
