package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.service.FacultyService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FacultyController.class)
class FacultyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FacultyService facultyService;

    @Test
    void testGetFacultyInfo() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Scarlet");

        when(facultyService.findFaculty(1L)).thenReturn(faculty);

        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Scarlet"));
    }

    @Test
    void testGetFacultyInfoNotFound() throws Exception {
        when(facultyService.findFaculty(999L)).thenReturn(null);

        mockMvc.perform(get("/faculty/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateFaculty() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setName("Slytherin");
        faculty.setColor("Green");

        Faculty savedFaculty = new Faculty();
        savedFaculty.setId(1L);
        savedFaculty.setName("Slytherin");
        savedFaculty.setColor("Green");

        when(facultyService.addFaculty(any(Faculty.class))).thenReturn(savedFaculty);

        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Slytherin"))
                .andExpect(jsonPath("$.color").value("Green"));
    }

    @Test
    void testEditFaculty() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Ravenclaw Updated");
        faculty.setColor("Blue");

        when(facultyService.editFaculty(any(Faculty.class))).thenReturn(faculty);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ravenclaw Updated"))
                .andExpect(jsonPath("$.color").value("Blue"));
    }

    @Test
    void testEditFacultyBadRequest() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Non-existent");
        faculty.setColor("Black");

        when(facultyService.editFaculty(any(Faculty.class))).thenReturn(null);

        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faculty)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteFaculty() throws Exception {
        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindFacultyByColor() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Hufflepuff");
        faculty.setColor("Yellow");

        when(facultyService.findByColor("Yellow")).thenReturn(faculty);

        mockMvc.perform(get("/faculty")
                        .param("color", "Yellow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hufflepuff"))
                .andExpect(jsonPath("$.color").value("Yellow"));
    }

    @Test
    void testFindFacultyByName() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Scarlet");

        when(facultyService.findByName("Gryffindor")).thenReturn(faculty);

        mockMvc.perform(get("/faculty")
                        .param("name", "Gryffindor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Scarlet"));
    }

    @Test
    void testFindFacultyByStudentName() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Scarlet");

        when(facultyService.findByStudentName("Harry Potter")).thenReturn(faculty);

        mockMvc.perform(get("/faculty")
                        .param("studentName", "Harry Potter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Scarlet"));
    }

    @Test
    void testFindFacultyWithNoParams() throws Exception {
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindFacultyWithEmptyParams() throws Exception {
        mockMvc.perform(get("/faculty")
                        .param("color", "")
                        .param("name", " ")
                        .param("studentName", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindFacultyWithNullColor() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Test Faculty");
        faculty.setColor("Blue");

        when(facultyService.findByName("Test Faculty")).thenReturn(faculty);

        mockMvc.perform(get("/faculty")
                        .param("color", (String) null)
                        .param("name", "Test Faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Faculty"));
    }
}
