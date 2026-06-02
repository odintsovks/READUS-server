package com.readus.forum.service;

import com.readus.forum.entity.Upload;
import com.readus.forum.repository.UploadRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final MinioClient minioClient;
    private final UploadRepository uploadRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.media.minio.bucket}")
    private String bucketName;

    @Value("${app.media.max-file-size}")
    private long maxFileSize;

    public Upload uploadFile(MultipartFile file, UUID userId, UUID messageId) throws Exception {
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File too large. Max 5MB");
        }

        String extension = getExtension(file.getOriginalFilename());
        String objectName = String.format("%s/%s/%s", userId, messageId,
                UUID.randomUUID() + extension);

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        Upload upload = new Upload();
        upload.setOriginalName(file.getOriginalFilename());
        upload.setStoredPath(objectName);
        upload.setMediaType(file.getContentType().startsWith("video/") ? 2 : 1);
        upload.setProcessingStatus(0);
        upload.setFileSize(file.getSize());

        upload = uploadRepository.save(upload);

        if (upload.getMediaType() == 2) {
            queueVideoProcessing(upload.getId(), objectName);
        }

        return upload;
    }

    public InputStream downloadFile(String path) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }

    public void queueVideoProcessing(UUID uploadId, String path) {
        Map<String, Object> event = Map.of(
                "uploadId", uploadId.toString(),
                "path", path,
                "timestamp", System.currentTimeMillis()
        );
        kafkaTemplate.send("video.process", uploadId.toString(), event);
        log.info("Queued video processing for upload: {}", uploadId);
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
