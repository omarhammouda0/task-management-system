package com.taskmanagement.attachment.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.auto-create-bucket}")
    private boolean autoCreateBucket;

    public void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists && autoCreateBucket) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("MinIO bucket '{}' created successfully", bucketName);
            }
        } catch (Exception e) {
            log.error("Error ensuring bucket exists", e);
            throw new RuntimeException("Failed to ensure MinIO bucket exists", e);
        }
    }

    public String uploadFile(MultipartFile file, String storedFilename) {
        try {
            ensureBucketExists();

            String objectKey = "attachments/" + storedFilename;

            minioClient.putObject(

                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded to MinIO: bucket={}, key={}", bucketName, objectKey);
            return objectKey;

        } catch (Exception e) {
            log.error("Error uploading file to MinIO", e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(

                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error downloading file from MinIO: {}", objectKey, e);
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            log.info("File deleted from MinIO: bucket={}, key={}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", objectKey, e);
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    public String generateStoredFilename(String originalFilename) {
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }
}
