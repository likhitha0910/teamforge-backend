package com.teamforge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public String fullName;
    @Column(nullable = false, unique = true)
    public String email;
    @JsonIgnore @Column(nullable = false)
    public String password; // BCrypt hash only
    public String department;
    @Column(name = "study_year")
    public Integer year;
    @Column(length = 500)
    public String skills;   // "Java, Spring Boot, MySQL"
    @Column(length = 1000)
    public String bio;
    public String role = "USER"; // USER or ADMIN
}
