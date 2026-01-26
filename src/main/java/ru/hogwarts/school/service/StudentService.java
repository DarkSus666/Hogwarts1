package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
public class StudentService {

    Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Value("${avatars.dir.path}")
    private String avatarsDir;

    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;

    public StudentService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
        logger.debug("StudentService создан");
    }

    public Student addStudent(Student student) {
        logger.info("Was invoked method for create student");
        logger.debug("Creating student: name={}, age={}", student.getName(), student.getAge());

        // Всегда устанавливаем id в null для новой записи
        student.setId(null);

        try {
            Student savedStudent = studentRepository.save(student);
            logger.info("Student created successfully with id={}", savedStudent.getId());
            return savedStudent;
        } catch (Exception e) {
            logger.error("Error creating student: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create student: " + e.getMessage(), e);
        }
    }

    public Student findStudent(long id) {
        logger.info("Was invoked method for find student");
        logger.debug("Searching for student with id={}", id);

        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Student with id={} not found", id);
                    logger.error("There is not student with id = " + id);
                    return new RuntimeException("Student not found with id: " + id);
                });
    }

    public List<Student> findAllStudents() {
        logger.info("Was invoked method for find all students");
        logger.debug("Retrieving all students");

        List<Student> students = studentRepository.findAll();
        logger.debug("Found {} students", students.size());
        return students;
    }

    public Student editStudent(Student student) {
        logger.info("Was invoked method for edit student");
        logger.debug("Editing student with id={}", student.getId());

        if (student.getId() == null) {
            logger.warn("Attempted to edit student without id");
            throw new IllegalArgumentException("Student id cannot be null for editing");
        }

        try {
            Student editedStudent = studentRepository.save(student);
            logger.info("Student with id={} edited successfully", student.getId());
            return editedStudent;
        } catch (Exception e) {
            logger.error("Error editing student with id={}: {}", student.getId(), e.getMessage(), e);
            throw e;
        }
    }

    public void deleteStudent(long id) {
        logger.info("Was invoked method for delete student");
        logger.debug("Deleting student with id={}", id);

        if (!studentRepository.existsById(id)) {
            logger.warn("Cannot delete student with id={} - not found", id);
            logger.error("There is not student with id = " + id);
            throw new RuntimeException("Student not found with id: " + id);
        }

        try {
            studentRepository.deleteById(id);
            logger.info("Student with id={} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting student with id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public Collection<Student> findByAge(int min, int max) {
        logger.info("Was invoked method for find student by age");
        logger.debug("Searching students with age between {} and {}", min, max);

        Collection<Student> students = studentRepository.findByAgeBetween(min, max);
        logger.debug("Found {} students with age between {} and {}", students.size(), min, max);
        return students;
    }

    public Collection<Student> findByFaculty(String facultyName) {
        logger.info("Was invoked method for find student by faculty");
        logger.debug("Searching students in faculty: {}", facultyName);

        if (facultyName == null || facultyName.trim().isEmpty()) {
            logger.warn("Faculty name is empty or null");
            throw new IllegalArgumentException("Faculty name cannot be empty");
        }

        Collection<Student> students = studentRepository.findStudentsByFaculty_Name(facultyName);
        logger.debug("Found {} students in faculty {}", students.size(), facultyName);
        return students;
    }

    public Avatar findAvatar(long studentId) {
        logger.info("Was invoked method for find avatar");
        logger.debug("Searching avatar for student id={}", studentId);

        return avatarRepository.findByStudentId(studentId)
                .orElseThrow(() -> {
                    logger.warn("Avatar not found for student id={}", studentId);
                    logger.error("There is not avatar for student with id = " + studentId);
                    return new RuntimeException("Avatar not found for student id: " + studentId);
                });
    }

    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method for upload avatar");
        logger.debug("Uploading avatar for student id={}, file size={}", studentId, file.getSize());

        if (file.isEmpty()) {
            logger.warn("Attempted to upload empty file for student id={}", studentId);
            throw new IllegalArgumentException("File is empty");
        }

        Student student = findStudent(studentId);

        Path filePath = Path.of(avatarsDir, studentId + "." + getExtension(file.getOriginalFilename()));
        logger.debug("Avatar file path: {}", filePath);

        try {
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);

            try (InputStream is = file.getInputStream();
                 OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                 BufferedInputStream bis = new BufferedInputStream(is, 1024);
                 BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
            ) {
                bis.transferTo(bos);
                logger.debug("Avatar file saved to disk: {}", filePath);
            }

            Avatar avatar = avatarRepository.findByStudentId(studentId).orElseGet(Avatar::new);
            avatar.setStudent(student);
            avatar.setFilePath(filePath.toString());
            avatar.setFileSize(file.getSize());
            avatar.setMediaType(file.getContentType());
            avatar.setData(file.getBytes());

            Avatar savedAvatar = avatarRepository.save(avatar);
            logger.info("Avatar uploaded successfully for student id={}, avatar id={}", studentId, savedAvatar.getId());

        } catch (IOException e) {
            logger.error("Error uploading avatar for student id={}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }

    private String getExtension(String fileName) {
        logger.debug("Getting extension for file: {}", fileName);

        if (fileName == null || fileName.isEmpty()) {
            logger.warn("File name is null or empty");
            throw new IllegalArgumentException("File name cannot be empty");
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            logger.warn("File '{}' has no extension", fileName);
            throw new IllegalArgumentException("File must have an extension");
        }

        String extension = fileName.substring(lastDotIndex + 1);
        logger.debug("File extension: {}", extension);
        return extension;
    }

    public Long getTotalStudentCount() {
        logger.info("Was invoked method for counting students");

        Long count = studentRepository.countAllStudents();
        logger.debug("Total student count: {}", count);
        return count;
    }

    public Double getAvgAgeOfStudents() {
        logger.info("Was invoked method for calculate average age of students");

        Double avgAge = studentRepository.getAvgAgeOfStudents();
        logger.debug("Average age of students: {}", avgAge);
        return avgAge;
    }

    public Collection<Student> getLastFiveStudents() {
        logger.info("Was invoked method for find last 5 students");

        Collection<Student> students = studentRepository.getLastFiveStudents();
        logger.debug("Found {} last students", students.size());
        return students;
    }
}