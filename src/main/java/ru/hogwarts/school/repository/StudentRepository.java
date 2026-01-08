package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.Student;

import java.util.Collection;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Collection<Student> findByAgeBetween(int min, int max);

    Collection<Student> findStudentsByFaculty_Name(String facultyName);

    @Query(value = "SELECT COUNT(*) FROM student", nativeQuery = true)
    Long countAllStudents();

    @Query(value = "SELECT AVG(age) FROM student ", nativeQuery = true)
    double getAvgAgeOfStudents();

    @Query(value = "SELECT * FROM student ORDER BY id DESC LIMIT 5", nativeQuery = true)
    Collection<Student> getLastFiveStudents();
}
