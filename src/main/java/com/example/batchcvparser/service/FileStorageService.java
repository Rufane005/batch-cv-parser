package com.example.batchcvparser.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileStorageService {

    private final Path rootPath = Paths.get("uploaded_cvs");

    public String unzipFile(MultipartFile zipFile) throws IOException {
        String uploadId = UUID.randomUUID().toString();
        Path uploadPath = rootPath.resolve(uploadId);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path filePath = uploadPath.resolve(Paths.get(entry.getName()).getFileName());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
        return uploadPath.toAbsolutePath().toString();
    }
}