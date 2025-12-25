package com.codelab.codelab.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
public class R2Config {

    private static final Dotenv dotenv = Dotenv.load(); // Loads .env file

    private static final String R2_ACCESS_KEY = dotenv.get("R2_ACCESS_KEY");
    private static final String R2_SECRET_KEY = dotenv.get("R2_SECRET_KEY");
    private static final String R2_ACCOUNT_ID = dotenv.get("R2_ACCOUNT_ID");
    private static final String R2_ENDPOINT = "https://" + R2_ACCOUNT_ID + ".r2.cloudflarestorage.com";

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(R2_ACCESS_KEY, R2_SECRET_KEY)))
                .endpointOverride(URI.create(R2_ENDPOINT))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .region(Region.US_EAST_1) // Cloudflare R2 does not use AWS regions, but this is required.
                .build();
    }
}

