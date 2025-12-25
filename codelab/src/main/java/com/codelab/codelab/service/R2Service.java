package com.codelab.codelab.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Service
public class R2Service {

    private final S3Client s3Client;
    private static String BUCKET_NAME = "sample-template";

    public R2Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public Path downloadFolder(String bucketName,String projectId, String projectType, String downloadDir) throws IOException {
        BUCKET_NAME = bucketName;
        Path extractDir = Path.of(downloadDir,projectId); // Destination folder

        // ✅ Ensure parent directory exists
        if (!Files.exists(extractDir)) {
            Files.createDirectories(extractDir);
        }

        // 🟢 Step 1: List all files in the folder
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .prefix(projectType.endsWith("/") ? projectType : projectType + "/") // Ensure folder path ends with "/"
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        for (S3Object s3Object : listResponse.contents()) {
            String objectKey = s3Object.key();
            // 🟢 Step 2: Download each file
            downloadFile(objectKey, projectId, extractDir);
        }

        return extractDir;
    }

    private void downloadFile(String objectKey, String projectId, Path baseDir) throws IOException {
        String[] secondPartOfPath = objectKey.split("/",2);
        Path filePath = baseDir.resolve(projectId + "/" + secondPartOfPath[1]);

        // ✅ Ensure parent directory exists before writing
        Files.createDirectories(filePath.getParent());

//        Path filePath = baseDir.toAbsolutePath();

        // 🟢 Fetch file from R2
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectKey)
                .build();

        try (InputStream responseStream = s3Client.getObject(getObjectRequest)) {
            Files.copy(responseStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}


