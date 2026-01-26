package ru.hogwarts.school.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Transactional
public class AvatarService {

    Logger logger = LoggerFactory.getLogger(AvatarService.class);

    @Value("${avatars.dir.path}")
    private String avatarsDir;

    private final StudentService studentService;
    private final AvatarRepository avatarRepository;

    public AvatarService(StudentService studentService, AvatarRepository avatarRepository) {
        this.studentService = studentService;
        this.avatarRepository = avatarRepository;
        logger.debug("AvatarService создан");
    }

    public Avatar findAvatar(Long studentId) {
        logger.info("Was invoked method for find avatar");
        logger.debug("Searching avatar for student id={}", studentId);

        try {
            Avatar avatar = avatarRepository.findByStudentId(studentId).orElse(new Avatar());

            if (avatar.getId() == null) {
                logger.warn("Avatar not found for student id={}, returning empty avatar", studentId);
                logger.debug("No avatar record for student id={}", studentId);
            } else {
                logger.debug("Found avatar: id={}, studentId={}, filePath={}, fileSize={} bytes",
                        avatar.getId(), studentId, avatar.getFilePath(), avatar.getFileSize());
            }

            return avatar;
        } catch (Exception e) {
            logger.error("Error finding avatar for student id={}: {}", studentId, e.getMessage(), e);
            return new Avatar();
        }
    }

    public Collection<Avatar> getAllAvatars(Integer pageNumber, Integer pageSize) {
        logger.info("Was invoked method for get all avatars");
        logger.debug("Getting avatars - pageNumber={}, pageSize={}", pageNumber, pageSize);

        if (pageNumber == null || pageNumber < 1) {
            logger.warn("Invalid pageNumber: {}, defaulting to 1", pageNumber);
            pageNumber = 1;
        }

        if (pageSize == null || pageSize < 1) {
            logger.warn("Invalid pageSize: {}, defaulting to 10", pageSize);
            pageSize = 10;
        }

        try {
            PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
            Collection<Avatar> avatars = avatarRepository.findAll(pageRequest).getContent();

            logger.debug("Retrieved {} avatars from page {}", avatars.size(), pageNumber);
            logger.info("Successfully retrieved {} avatars", avatars.size());

            return avatars;
        } catch (Exception e) {
            logger.error("Error getting avatars: pageNumber={}, pageSize={}, error: {}",
                    pageNumber, pageSize, e.getMessage(), e);
            throw new RuntimeException("Failed to get avatars: " + e.getMessage(), e);
        }
    }

    public void uploadAvatar(Long studentId, MultipartFile avatarFile) throws IOException {
        logger.info("Was invoked method for upload avatar");
        logger.debug("Uploading avatar for student id={}, file name={}, size={} bytes",
                studentId, avatarFile.getOriginalFilename(), avatarFile.getSize());

        if (avatarFile.isEmpty()) {
            logger.warn("Attempt to upload empty file for student id={}", studentId);
            throw new IllegalArgumentException("Avatar file cannot be empty");
        }

        // Проверка типа файла
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warn("Invalid file type for student id={}: {}", studentId, contentType);
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Проверка размера файла (максимум 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (avatarFile.getSize() > maxSize) {
            logger.warn("File too large for student id={}: {} bytes (max {} bytes)",
                    studentId, avatarFile.getSize(), maxSize);
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        try {
            Student student = studentService.findStudent(studentId);
            logger.debug("Found student: id={}, name={}", student.getId(), student.getName());

            String extension = getExtensions(avatarFile.getOriginalFilename());
            Path filePath = Path.of(avatarsDir, studentId + "." + extension);

            logger.debug("Avatar file path: {}", filePath);

            // Создаем директории, если не существуют
            Files.createDirectories(filePath.getParent());
            logger.debug("Created directories for path: {}", filePath.getParent());

            // Удаляем старый файл, если существует
            if (Files.exists(filePath)) {
                Files.deleteIfExists(filePath);
                logger.debug("Deleted existing avatar file: {}", filePath);
            }

            // Копируем файл
            try (
                    InputStream is = avatarFile.getInputStream();
                    OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                    BufferedInputStream bis = new BufferedInputStream(is, 1024);
                    BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
            ) {
                long bytesCopied = bis.transferTo(bos);
                logger.debug("Copied {} bytes to file: {}", bytesCopied, filePath);
            } catch (IOException e) {
                logger.error("Error copying avatar file for student id={}: {}", studentId, e.getMessage(), e);
                throw new IOException("Failed to save avatar file", e);
            }

            Avatar avatar = findAvatar(studentId);

            if (avatar.getId() == null) {
                logger.debug("Creating new avatar record for student id={}", studentId);
            } else {
                logger.debug("Updating existing avatar id={} for student id={}", avatar.getId(), studentId);
            }

            avatar.setStudent(student);
            avatar.setFilePath(filePath.toString());
            avatar.setFileSize(avatarFile.getSize());
            avatar.setMediaType(avatarFile.getContentType());
            avatar.setData(avatarFile.getBytes());

            Avatar savedAvatar = avatarRepository.save(avatar);
            logger.info("Avatar uploaded successfully for student id={}, avatar id={}, path={}",
                    studentId, savedAvatar.getId(), savedAvatar.getFilePath());

        } catch (Exception e) {
            logger.error("Error uploading avatar for student id={}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }

    private String getExtensions(String fileName) {
        logger.debug("Getting extension for file: {}", fileName);

        if (fileName == null || fileName.isEmpty()) {
            logger.error("File name is null or empty");
            throw new IllegalArgumentException("File name cannot be empty");
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            logger.error("File '{}' has no valid extension", fileName);
            throw new IllegalArgumentException("File must have a valid extension");
        }

        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        logger.debug("File extension: {}", extension);
        return extension;
    }

}