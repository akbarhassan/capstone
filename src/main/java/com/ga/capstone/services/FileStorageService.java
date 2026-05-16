package com.ga.capstone.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service responsible for storing and deleting uploaded files on the local filesystem.
 * Files are saved under the configured upload directory in sub-folders by entity type.
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.path:./uploads}")
    private String uploadDir;

    /**
     * Save a multipart file to disk under the specified sub-path.
     *
     * @param file     the uploaded file
     * @param entityId the ID of the entity this file belongs to (used in filename)
     * @param subPath  the sub-directory (e.g., "profiles", "courses", "categories")
     * @return the generated filename
     * @throws RuntimeException if file saving fails
     */
    public String saveFile(MultipartFile file, Long entityId, String subPath) {
        try {
            Path targetPath = Paths.get(uploadDir, subPath);
            Files.createDirectories(targetPath);

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String fileName = entityId + "_" + System.currentTimeMillis() + "." + extension;

            // Validate file content type
            String contentType = file.getContentType();
            if (!List.of("image/jpeg", "image/png", "image/gif").contains(contentType)) {
                throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and GIF are allowed.");
            }

            file.transferTo(Paths.get(targetPath.toString(), fileName));
            return fileName;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to upload file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete a file from disk.
     *
     * @param fileName the name of the file to delete
     * @param subPath  the sub-directory the file is stored in
     */
    public void deleteFile(String fileName, String subPath) {
        try {
            Path filePath = Paths.get(uploadDir, subPath, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }
}
