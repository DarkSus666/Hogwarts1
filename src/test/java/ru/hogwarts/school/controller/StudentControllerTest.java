package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.AvatarService;
import ru.hogwarts.school.service.StudentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private StudentService studentService;

    @MockBean
    private AvatarService avatarService;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/student";
    }

    @Test
    void testGetStudentInfo_Success() {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setName("Harry Potter");
        student.setAge(17);

        when(studentService.findStudent(1L)).thenReturn(student);

        // Act
        ResponseEntity<Student> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}", Student.class, 1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getName()).isEqualTo("Harry Potter");
        assertThat(response.getBody().getAge()).isEqualTo(17);
    }

    @Test
    void testGetStudentInfo_NotFound() {
        // Arrange
        when(studentService.findStudent(999L)).thenReturn(null);

        // Act
        ResponseEntity<Student> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}", Student.class, 999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testCreateStudent_Success() {
        // Arrange
        Student student = new Student();
        student.setName("Hermione Granger");
        student.setAge(17);

        Student createdStudent = new Student();
        createdStudent.setId(2L);
        createdStudent.setName("Hermione Granger");
        createdStudent.setAge(17);

        when(studentService.addStudent(any(Student.class))).thenReturn(createdStudent);

        // Act
        ResponseEntity<Student> response = restTemplate.postForEntity(
                getBaseUrl(),
                student,
                Student.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(2L);
        assertThat(response.getBody().getName()).isEqualTo("Hermione Granger");
        assertThat(response.getBody().getAge()).isEqualTo(17);
    }

    @Test
    void testEditStudent_Success() {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setName("Ron Weasley");
        student.setAge(17);

        Student updatedStudent = new Student();
        updatedStudent.setId(1L);
        updatedStudent.setName("Ron Weasley");
        updatedStudent.setAge(17);

        when(studentService.editStudent(any(Student.class))).thenReturn(updatedStudent);

        // Act
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> requestEntity = new HttpEntity<>(student, headers);

        ResponseEntity<Student> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                requestEntity,
                Student.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getName()).isEqualTo("Ron Weasley");
        assertThat(response.getBody().getAge()).isEqualTo(17);
    }

    @Test
    void testEditStudent_BadRequest() {
        // Arrange
        Student student = new Student();
        student.setId(999L);
        student.setName("Unknown");
        student.setAge(20);

        when(studentService.editStudent(any(Student.class))).thenReturn(null);

        // Act
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Student> requestEntity = new HttpEntity<>(student, headers);

        ResponseEntity<Student> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                requestEntity,
                Student.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testDeleteStudent_Success() {
        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testFindStudentsByAgeRange() {
        // Arrange
        Student student1 = new Student();
        student1.setId(1L);
        student1.setName("Harry Potter");
        student1.setAge(17);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setName("Hermione Granger");
        student2.setAge(17);

        List<Student> students = Arrays.asList(student1, student2);

        when(studentService.findByAge(16, 18)).thenReturn(students);

        // Act
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                getBaseUrl() + "?min={min}&max={max}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {},
                16, 18);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Harry Potter");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Hermione Granger");
    }

    @Test
    void testFindStudentsByFacultyName() {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setName("Harry Potter");
        student.setAge(17);

        List<Student> students = Collections.singletonList(student);

        when(studentService.findByFaculty("Gryffindor")).thenReturn(students);

        // Act
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                getBaseUrl() + "?facultyName={facultyName}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {},
                "Gryffindor");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Harry Potter");
        assertThat(response.getBody().get(0).getAge()).isEqualTo(17);
    }

    @Test
    void testFindStudents_NoParams_EmptyList() {
        // Act
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {});

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void testDownloadAvatarPreview_Success() {
        // Arrange
        Avatar avatar = new Avatar();
        avatar.setId(1L);
        avatar.setMediaType("image/jpeg");
        avatar.setData("test image data".getBytes());

        when(avatarService.findAvatar(1L)).thenReturn(avatar);

        // Act
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}/avatar/preview",
                byte[].class,
                1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.parseMediaType("image/jpeg"));
        assertThat(response.getHeaders().getContentLength()).isPositive();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testDownloadAvatar_NotFound() {
        // Arrange
        when(avatarService.findAvatar(999L)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}/avatar",
                Void.class,
                999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDownloadAvatarPreview_NotFound() {
        // Arrange
        when(avatarService.findAvatar(999L)).thenReturn(null);

        // Act
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}/avatar/preview",
                byte[].class,
                999L);

        // Assert
    }

    @Test
    void testFindStudentsWithInvalidAgeParameters() {
        // Act
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                getBaseUrl() + "?min={min}&max={max}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {},
                20, 10); // min > max

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Метод вернет пустой список, так как min > max
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testFindStudentsWithNegativeAge() {
        // Act
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                getBaseUrl() + "?min={min}&max={max}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {},
                -5, -1);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Метод вернет пустой список, так как возраст отрицательный
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testGetStudent_WithInvalidId() {
        // Act
        ResponseEntity<Student> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}",
                Student.class,
                "invalid"); // Не число

        // Assert
        // Будет ошибка 400, так как id должен быть числом
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}