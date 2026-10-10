package com.teamforge.dto;

import com.teamforge.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {
    private Dtos() {}
    public record RegisterReq(String fullName, String email, String password, String department, Integer year, String skills, String bio) {}
    public record LoginReq(String email, String password) {}
    public record ProfileReq(String fullName, String email, String department, Integer year, String skills, String bio) {}
    public record ProjectReq(String title, String description, String requiredSkills, Integer teamSize, String status) {}
    public record JoinReq(String message) {}
    public record ProjectView(Long id, String title, String description, String requiredSkills, Integer teamSize,
                              String status, Long ownerId, String ownerName, int members, LocalDateTime createdAt,
                              List<User> team, String myRequest, boolean mine) {}
    public record ReqView(Long id, Long projectId, String projectTitle, Long userId, String userName,
                          String userSkills, String message, String status, LocalDateTime createdAt) {}
    public record ProjRec(ProjectView project, int score, List<String> matched) {}
    public record StuRec(User student, Long projectId, String projectTitle, int score, List<String> matched) {}
}
