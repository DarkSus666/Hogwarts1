package ru.hogwarts.school.model;

import jakarta.persistence.*;

import java.util.Arrays;

@Entity
@Table(name = "avatar")
public class Avatar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;
    private long fileSize;
    private String mediaType;

    @Lob
    private byte[] data;

    @OneToOne
    private Student student;

    public Avatar() {
    }

    public Avatar(Student student, byte[] data, String mediaType, long fileSize, String filePath, Long id) {
        this.student = student;
        this.data = data;
        this.mediaType = mediaType;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "Avatar{" + "id=" + id + ", filePath='" + filePath + '\'' + ", fileSize=" + fileSize + ", mediaType='" + mediaType + '\'' + ", data=" + Arrays.toString(data) + ", student=" + student + '}';
    }
}
