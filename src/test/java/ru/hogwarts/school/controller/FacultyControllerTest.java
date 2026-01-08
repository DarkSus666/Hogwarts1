package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.service.FacultyService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private FacultyService facultyService;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/faculty";
    }

    @Test
    void testGetFacultyInfo_Success() {
        // Arrange
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        when(facultyService.findFaculty(1L)).thenReturn(faculty);

        // Act
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}", Faculty.class, 1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getName()).isEqualTo("Gryffindor");
        assertThat(response.getBody().getColor()).isEqualTo("Red");
    }

    @Test
    void testGetFacultyInfo_NotFound() {
        // Arrange
        when(facultyService.findFaculty(999L)).thenReturn(null);

        // Act
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl() + "/{id}", Faculty.class, 999L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testCreateFaculty_Success() {
        // Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Slytherin");
        faculty.setColor("Green");

        Faculty createdFaculty = new Faculty();
        createdFaculty.setId(2L);
        createdFaculty.setName("Slytherin");
        createdFaculty.setColor("Green");

        when(facultyService.addFaculty(any(Faculty.class))).thenReturn(createdFaculty);

        // Act
        ResponseEntity<Faculty> response = restTemplate.postForEntity(
                getBaseUrl(),
                faculty,
                Faculty.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(2L);
        assertThat(response.getBody().getName()).isEqualTo("Slytherin");
        assertThat(response.getBody().getColor()).isEqualTo("Green");
    }

    @Test
    void testEditFaculty_Success() {
        // Arrange
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Ravenclaw");
        faculty.setColor("Blue");

        Faculty updatedFaculty = new Faculty();
        updatedFaculty.setId(1L);
        updatedFaculty.setName("Ravenclaw");
        updatedFaculty.setColor("Blue");

        when(facultyService.editFaculty(any(Faculty.class))).thenReturn(updatedFaculty);

        // Act
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(faculty, headers);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                requestEntity,
                Faculty.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getName()).isEqualTo("Ravenclaw");
        assertThat(response.getBody().getColor()).isEqualTo("Blue");
    }

    @Test
    void testEditFaculty_BadRequest() {
        // Arrange
        Faculty faculty = new Faculty();
        faculty.setId(999L);
        faculty.setName("Unknown");
        faculty.setColor("Black");

        when(facultyService.editFaculty(any(Faculty.class))).thenReturn(null);

        // Act
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(faculty, headers);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.PUT,
                requestEntity,
                Faculty.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testDeleteFaculty_Success() {
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
    void testFindFacultyByColor() {
        // Arrange
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        when(facultyService.findByColor("Red")).thenReturn(faculty);

        // Act
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl() + "?color={color}",
                Faculty.class,
                "Red");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getColor()).isEqualTo("Red");
        assertThat(response.getBody().getName()).isEqualTo("Gryffindor");
    }

    @Test
    void testFindFacultyByName() {
        // Arrange
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        when(facultyService.findByName("Gryffindor")).thenReturn(faculty);

        // Act
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl() + "?name={name}",
                Faculty.class,
                "Gryffindor");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Gryffindor");
        assertThat(response.getBody().getColor()).isEqualTo("Red");
    }

    @Test
    void testFindFacultyByStudentName() {
        // Arrange
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        when(facultyService.findByStudentName("Harry Potter")).thenReturn(faculty);

        // Act
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl() + "?studentName={studentName}",
                Faculty.class,
                "Harry Potter");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Gryffindor");
        assertThat(response.getBody().getColor()).isEqualTo("Red");
    }

    @Test
    void testFindFaculty_NoParams_BadRequest() {
        // Act
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                getBaseUrl(),
                Faculty.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}