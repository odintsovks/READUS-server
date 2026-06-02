package com.readus.forum.service;

import com.readus.forum.entity.Upload;
import com.readus.forum.repository.UploadRepository;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessorService {

    private final MinioClient minioClient;
    private final UploadRepository uploadRepository;

    @Value("${app.media.minio.bucket}")
    private String bucketName;

    @Value("${app.media.temp-dir:/tmp/readus-media}")
    private String tempDir;

    public void generatePreview(UUID uploadId, String inputPath) throws Exception {
        Files.createDirectories(Paths.get(tempDir));

        String tempInputFile = tempDir + "/input_" + UUID.randomUUID() + ".mp4";
        String tempOutputFile = tempDir + "/preview_" + UUID.randomUUID() + ".mp4";

        // Download from MinIO
        try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(inputPath)
                .build());
             FileOutputStream fos = new FileOutputStream(tempInputFile)) {
            is.transferTo(fos);
        }

        // Generate preview with FFmpeg
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", tempInputFile,
                "-ss", "00:00:00",
                "-t", "5",
                "-vf", "scale=640:-1",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-preset", "fast",
                "-crf", "28",
                "-y",
                tempOutputFile
        );

        log.info("Running FFmpeg: {}", String.join(" ", pb.command()));
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
                throw new RuntimeException("FFmpeg failed: " + error);
            }
        }

        // Upload preview to MinIO
        String previewPath = "previews/" + UUID.randomUUID() + ".mp4";
        try (FileInputStream fis = new FileInputStream(tempOutputFile)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(previewPath)
                    .stream(fis, Files.size(Path.of(tempOutputFile)), -1)
                    .contentType("video/mp4")
                    .build());
        }

        // Update upload entity
        Upload upload = uploadRepository.findById(uploadId).orElse(null);
        if (upload != null) {
            upload.setPreviewPath(previewPath);
            upload.setProcessingStatus(1);
            uploadRepository.save(upload);
        }

        // Cleanup
        Files.deleteIfExists(Path.of(tempInputFile));
        Files.deleteIfExists(Path.of(tempOutputFile));

        log.info("Preview generated for upload: {}", uploadId);
    }
}