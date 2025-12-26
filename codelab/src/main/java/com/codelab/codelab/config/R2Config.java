package com.codelab.codelab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "r2.enabled", havingValue = "true")
public class R2Config {

    @Value("${r2.token.access-key}")
    private String R2_ACCESS_KEY;

    @Value("${r2.token.secret-key}")
    private String R2_SECRET_KEY;

    @Value("${r2.token.account-id}")
    private String R2_ACCOUNT_ID;



    @Bean
    public S3Client s3Client() {
        if (R2_ACCOUNT_ID == null || R2_ACCOUNT_ID.isBlank()) {
            throw new IllegalStateException("r2.token.account-id is missing");
        }
        String R2_ENDPOINT = "https://" + R2_ACCOUNT_ID.trim() + ".r2.cloudflarestorage.com";

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(R2_ACCESS_KEY, R2_SECRET_KEY)))
                .endpointOverride(URI.create(R2_ENDPOINT))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .region(Region.US_EAST_1) // Cloudflare R2 does not use AWS regions, but this is required.
                .build();
    }
}

