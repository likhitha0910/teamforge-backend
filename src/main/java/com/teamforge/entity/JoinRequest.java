package com.teamforge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "join_requests")
public class JoinRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "project_id", nullable = false)
    public Project project;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "user_id", nullable = false)
    public User user;
    @Column(length = 500)
    public String message;
    public String status = "PENDING"; // PENDING, ACCEPTED, REJECTED
    public LocalDateTime createdAt = LocalDateTime.now();
}
