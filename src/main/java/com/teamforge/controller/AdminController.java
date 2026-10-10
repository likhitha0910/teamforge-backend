package com.teamforge.controller;

import com.teamforge.dto.Dtos.ProjectView;
import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import com.teamforge.exception.Err;
import com.teamforge.repository.JoinRequestRepository;
import com.teamforge.repository.ProjectRepository;
import com.teamforge.repository.UserRepository;
import com.teamforge.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final ProjectRepository projects;
    private final JoinRequestRepository reqs;
    private final ProjectService svc;

    public AdminController(UserRepository users, ProjectRepository projects, JoinRequestRepository reqs, ProjectService svc) {
        this.users = users; this.projects = projects; this.reqs = reqs; this.svc = svc;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        long pending = reqs.findAll().stream().filter(r -> "PENDING".equals(r.status)).count();
        return Map.of("users", users.count(), "projects", projects.count(), "requests", reqs.count(), "pending", pending);
    }

    @GetMapping("/users")
    public List<User> listUsers() { return users.findAll(); }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(Authentication a, @PathVariable Long id) {
        User me = svc.me(a);
        User u = users.findById(id).orElseThrow(() -> Err.notFound("User not found"));
        if (u.id.equals(me.id) || "ADMIN".equals(u.role)) throw Err.bad("Admin accounts cannot be deleted");
        for (Project p : projects.findByOwnerOrderByCreatedAtDesc(u)) {
            reqs.deleteByProject(p);
            projects.delete(p);
        }
        reqs.deleteByUser(u);
        users.delete(u);
        return Map.of("deleted", true);
    }

    @GetMapping("/projects")
    public List<ProjectView> listProjects(Authentication a) {
        User me = svc.me(a);
        return projects.findAllByOrderByCreatedAtDesc().stream().map(p -> svc.view(p, me)).toList();
    }

    @DeleteMapping("/projects/{id}")
    public Map<String, Object> deleteProject(@PathVariable Long id) {
        Project p = projects.findById(id).orElseThrow(() -> Err.notFound("Project not found"));
        reqs.deleteByProject(p);
        projects.delete(p);
        return Map.of("deleted", true);
    }
}
