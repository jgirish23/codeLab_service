package com.codelab.codelab.controller;

import com.codelab.codelab.service.R2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/r2")
public class R2Controller {

    private final R2Service r2Service;

    public R2Controller(R2Service r2Service) {
        this.r2Service = r2Service;
    }

    @GetMapping("/downloadTemplate")
    public ResponseEntity<String> downloadZip(
            @RequestParam String folderName,
            @RequestParam String projectType,
            @RequestParam String projectId
    ) throws IOException {
        String downloadDir = "project";
        try {
            Path downloadedFile = r2Service.downloadFolder(folderName, projectId, projectType, downloadDir);
            return ResponseEntity.ok("Project downloaded successfully: " + projectId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error downloading file: " + e.getMessage());
        }
    }
}


