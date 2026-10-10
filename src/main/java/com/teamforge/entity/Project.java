package com.teamforge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public String title;
    @Column(length = 2000)
    public String description;
    @Column(length = 500)
    public String requiredSkills;
    public Integer teamSize;
    public String status = "OPEN"; // OPEN, IN_PROGRESS, COMPLETED, CLOSED
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "owner_id", nullable = false)
    public User owner;
    public LocalDateTime createdAt = LocalDateTime.now();
}
