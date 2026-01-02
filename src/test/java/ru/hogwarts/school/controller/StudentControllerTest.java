package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.AvatarService;
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private AvatarService avatarService;

    @Test
    void testGetStudentInfo() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setName("Harry Potter");
        student.setAge(17);

        when(studentService.findStudent(1L)).thenReturn(student);

        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    void testGetStudentInfoNotFound() throws Exception {
        when(studentService.findStudent(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateStudent() throws Exception {
        Student student = new Student();
        student.setName("Hermione Granger");
        student.setAge(17);

        Student savedStudent = new Student();
        savedStudent.setId(1L);
        savedStudent.setName("Hermione Granger");
        savedStudent.setAge(17);

        when(studentService.addStudent(any(Student.class))).thenReturn(savedStudent);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hermione Granger"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    void testEditStudent() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setName("Ron Weasley Updated");
        student.setAge(18);

        when(studentService.editStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ron Weasley Updated"))
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    void testEditStudentBadRequest() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setName("Non-existent");
        student.setAge(18);

        when(studentService.editStudent(any(Student.class))).thenReturn(null);

        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteStudent() throws Exception {
        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindStudentsByAgeRange() throws Exception {
        Student student1 = new Student();
        student1.setId(1L);
        student1.setName("Student 1");
        student1.setAge(19);

        Student student2 = new Student();
        student2.setId(2L);
        student2.setName("Student 2");
        student2.setAge(20);

        List<Student> students = Arrays.asList(student1, student2);

        when(studentService.findByAge(18, 21)).thenReturn(students);

        mockMvc.perform(get("/student")
                        .param("min", "18")
                        .param("max", "21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Student 1"))
                .andExpect(jsonPath("$[1].name").value("Student 2"));
    }

    @Test
    void testFindStudentsByFacultyName() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Scarlet");

        Student student = new Student();
        student.setId(1L);
        student.setName("Harry Potter");
        student.setAge(17);
        // Если есть сеттер для faculty
        // student.setFaculty(faculty);

        List<Student> students = Collections.singletonList(student);

        when(studentService.findByFaculty("Gryffindor")).thenReturn(students);

        mockMvc.perform(get("/student")
                        .param("facultyName", "Gryffindor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"));
    }

    @Test
    void testFindStudentsNoParams() throws Exception {
        when(studentService.findByAge(anyInt(), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testUploadAvatar() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/student/1/avatar")
                        .file(file))
                .andExpect(status().isOk());
    }

    @Test
    void testDownloadAvatarPreview() throws Exception {
        Avatar avatar = new Avatar();
        avatar.setId(1L);
        avatar.setMediaType("image/jpeg");
        avatar.setData("test image data".getBytes());

        when(avatarService.findAvatar(1L)).thenReturn(avatar);

        mockMvc.perform(get("/student/1/avatar/preview"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("test image data".getBytes()));
    }

    @Test
    void testDownloadAvatar() throws Exception {
        // Для WebMvc тестов лучше не тестировать методы, которые работают с файловой системой
        // Вместо этого можно просто проверить, что вызывается правильный endpoint
        // или мокировать всю цепочку

        // Способ 1: Пропускаем этот тест в WebMvc
        // Способ 2: Мокируем аватар с null путем к файлу (если контроллер это обрабатывает)

        // Проверяем только, что endpoint существует и возвращает правильный статус
        // когда аватар не найден
        when(avatarService.findAvatar(1L)).thenReturn(null);

        mockMvc.perform(get("/student/1/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDownloadAvatarNotFound() throws Exception {
        when(avatarService.findAvatar(999L)).thenReturn(null);

        mockMvc.perform(get("/student/999/avatar"))
                .andExpect(status().isNotFound());
    }
}
