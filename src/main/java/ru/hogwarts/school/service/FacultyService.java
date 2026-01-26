package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Optional;

@Service
public class FacultyService {

    Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
        logger.debug("FacultyService создан");
    }

    public Faculty addFaculty(Faculty faculty) {
        logger.info("Was invoked method for create faculty");
        logger.debug("Creating faculty: name={}, color={}", faculty.getName(), faculty.getColor());

        try {
            Faculty savedFaculty = facultyRepository.save(faculty);
            logger.info("Faculty created successfully with id={}, name={}", savedFaculty.getId(), savedFaculty.getName());
            return savedFaculty;
        } catch (Exception e) {
            logger.error("Error creating faculty: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create faculty: " + e.getMessage(), e);
        }
    }

    public Faculty findFaculty(long id) {
        logger.info("Was invoked method for find faculty");
        logger.debug("Searching for faculty with id={}", id);

        Optional<Faculty> facultyOptional = facultyRepository.findById(id);

        if (facultyOptional.isEmpty()) {
            logger.warn("Faculty with id={} not found", id);
            logger.error("There is not faculty with id = " + id);
            throw new RuntimeException("Faculty not found with id: " + id);
        }

        Faculty faculty = facultyOptional.get();
        logger.debug("Found faculty: id={}, name={}, color={}", faculty.getId(), faculty.getName(), faculty.getColor());
        return faculty;
    }

    public Faculty editFaculty(Faculty faculty) {
        logger.info("Was invoked method for edit faculty");
        logger.debug("Editing faculty with id={}, name={}", faculty.getId(), faculty.getName());

        if (faculty.getId() == null) {
            logger.warn("Attempted to edit faculty without id");
            throw new IllegalArgumentException("Faculty id cannot be null for editing");
        }

        // Проверяем, существует ли факультет с таким ID
        if (!facultyRepository.existsById(faculty.getId())) {
            logger.warn("Cannot edit faculty with id={} - not found", faculty.getId());
            logger.error("There is not faculty with id = " + faculty.getId());
            throw new RuntimeException("Faculty not found with id: " + faculty.getId());
        }

        try {
            Faculty editedFaculty = facultyRepository.save(faculty);
            logger.info("Faculty with id={} edited successfully", faculty.getId());
            return editedFaculty;
        } catch (Exception e) {
            logger.error("Error editing faculty with id={}: {}", faculty.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to edit faculty: " + e.getMessage(), e);
        }
    }

    public void deleteFaculty(long id) {
        logger.info("Was invoked method for delete faculty");
        logger.debug("Deleting faculty with id={}", id);

        if (!facultyRepository.existsById(id)) {
            logger.warn("Cannot delete faculty with id={} - not found", id);
            logger.error("There is not faculty with id = " + id);
            throw new RuntimeException("Faculty not found with id: " + id);
        }

        try {
            facultyRepository.deleteById(id);
            logger.info("Faculty with id={} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting faculty with id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete faculty: " + e.getMessage(), e);
        }
    }

    public Faculty findByColor(String color) {
        logger.info("Was invoked method for find faculty by color");
        logger.debug("Searching faculty with color: {}", color);

        if (color == null || color.trim().isEmpty()) {
            logger.warn("Color parameter is empty or null");
            throw new IllegalArgumentException("Color cannot be empty");
        }

        Faculty faculty = facultyRepository.findFacultyByColorIgnoreCase(color);

        if (faculty == null) {
            logger.warn("No faculty found with color: {}", color);
            logger.debug("Faculty with color '{}' not found", color);
        } else {
            logger.debug("Found faculty: id={}, name={}, color={}",
                    faculty.getId(), faculty.getName(), faculty.getColor());
        }

        return faculty;
    }

    public Faculty findByName(String name) {
        logger.info("Was invoked method for find faculty by name");
        logger.debug("Searching faculty with name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            logger.warn("Name parameter is empty or null");
            throw new IllegalArgumentException("Name cannot be empty");
        }

        Faculty faculty = facultyRepository.findFacultyByNameIgnoreCase(name);

        if (faculty == null) {
            logger.warn("No faculty found with name: {}", name);
            logger.debug("Faculty with name '{}' not found", name);
        } else {
            logger.debug("Found faculty: id={}, name={}, color={}",
                    faculty.getId(), faculty.getName(), faculty.getColor());
        }

        return faculty;
    }

    public Faculty findByStudentName(String studentName) {
        logger.info("Was invoked method for find faculty by student name");
        logger.debug("Searching faculty for student: {}", studentName);

        if (studentName == null || studentName.trim().isEmpty()) {
            logger.warn("Student name parameter is empty or null");
            throw new IllegalArgumentException("Student name cannot be empty");
        }

        Faculty faculty = facultyRepository.findByStudents_Name(studentName);

        if (faculty == null) {
            logger.warn("No faculty found for student: {}", studentName);
            logger.debug("Faculty for student '{}' not found", studentName);
        } else {
            logger.debug("Found faculty: id={}, name={} for student: {}",
                    faculty.getId(), faculty.getName(), studentName);
        }

        return faculty;
    }
}