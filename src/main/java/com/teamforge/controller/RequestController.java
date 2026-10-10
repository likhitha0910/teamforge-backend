package com.teamforge.controller;

import com.teamforge.dto.Dtos.ReqView;
import com.teamforge.entity.JoinRequest;
import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import com.teamforge.exception.Err;
import com.teamforge.repository.JoinRequestRepository;
import com.teamforge.repository.ProjectRepository;
import com.teamforge.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final JoinRequestRepository reqs;
    private final ProjectRepository projects;
    private final ProjectService svc;

    public RequestController(JoinRequestRepository reqs, ProjectRepository projects, ProjectService svc) {
        this.reqs = reqs; this.projects = projects; this.svc = svc;
    }

    private static ReqView v(JoinRequest j) {
        return new ReqView(j.id, j.project.id, j.project.title, j.user.id, j.user.fullName, j.user.skills,
                j.message, j.status, j.createdAt);
    }

    private JoinRequest find(Long id) { return reqs.findById(id).orElseThrow(() -> Err.notFound("Request not found")); }

    /** requests received on projects I own */
    @GetMapping("/incoming")
    public List<ReqView> incoming(Authentication a) {
        return reqs.findByProjectOwnerOrderByCreatedAtDesc(svc.me(a)).stream().map(RequestController::v).toList();
    }

    /** requests I have sent */
    @GetMapping("/outgoing")
    public List<ReqView> outgoing(Authentication a) {
        return reqs.findByUserOrderByCreatedAtDesc(svc.me(a)).stream().map(RequestController::v).toList();
    }

    @PutMapping("/{id}/{action}")
    public ReqView decide(Authentication a, @PathVariable Long id, @PathVariable String action) {
        User me = svc.me(a);
        JoinRequest j = find(id);
        Project p = j.project;
        if (!p.owner.id.equals(me.id)) throw Err.forbidden("Only the project owner can answer requests");
        if (!"PENDING".equals(j.status)) throw Err.bad("This request was already answered");
        if ("accept".equals(action)) {
            if (svc.team(p).size() >= p.teamSize) throw Err.bad("The team is already full");
            j.status = "ACCEPTED";
            reqs.save(j);
            if (svc.team(p).size() >= p.teamSize && "OPEN".equals(p.status)) {
                p.status = "IN_PROGRESS";
                projects.save(p);
            }
        } else if ("reject".equals(action)) {
            j.status = "REJECTED";
            reqs.save(j);
        } else {
            throw Err.bad("Unknown action");
        }
        return v(j);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> cancel(Authentication a, @PathVariable Long id) {
        User me = svc.me(a);
        JoinRequest j = find(id);
        if (!j.user.id.equals(me.id)) throw Err.forbidden("This is not your request");
        if (!"PENDING".equals(j.status)) throw Err.bad("Only pending requests can be cancelled");
        reqs.delete(j);
        return Map.of("deleted", true);
    }
}
