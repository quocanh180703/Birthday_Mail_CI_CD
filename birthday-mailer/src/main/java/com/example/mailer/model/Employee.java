package com.example.mailer.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Employee {
    private String name;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dob;
    private String imagePath;

    public Employee() {}

    public Employee(String name, String email, LocalDate dob, String imagePath) {
        this.name = name;
        this.email = email;
        this.dob = dob;
        this.imagePath = imagePath;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
