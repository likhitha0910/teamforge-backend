package com.teamforge.controller;

import com.teamforge.dto.Dtos.*;
import com.teamforge.entity.JoinRequest;
import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import com.teamforge.exception.Err;
import com.teamforge.repository.JoinRequestRepository;
import com.teamforge.repository.ProjectRepository;
import com.teamforge.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private static final Set<String> STATUSES = Set.of("OPEN", "IN_PROGRESS", "COMPLETED", "CLOSED");
    private final ProjectRepository projects;
    private final JoinRequestRepository reqs;
    private final ProjectService svc;

    public ProjectController(ProjectRepository projects, JoinRequestRepository reqs, ProjectService svc) {
        this.projects = projects; this.reqs = reqs; this.svc = svc;
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private Project find(Long id) { return projects.findById(id).orElseThrow(() -> Err.notFound("Project not found")); }

    private void apply(Project p, ProjectReq r) {
        if (r.title() == null || r.title().isBlank()) throw Err.bad("Project title cannot be empty");
        if (r.teamSize() == null || r.teamSize() < 1) throw Err.bad("Team size must be at least 1");
        p.title = r.title().trim();
        p.description = r.description();
        p.requiredSkills = r.requiredSkills();
        p.teamSize = r.teamSize();
        p.status = (r.status() != null && STATUSES.contains(r.status())) ? r.status() : "OPEN";
    }

    @GetMapping
    public List<ProjectView> list(Authentication a, @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String skill, @RequestParam(required = false) String status) {
        User me = svc.me(a);
        String qq = nz(q).toLowerCase().trim(), sk = nz(skill).toLowerCase().trim(), st = nz(status).trim();
        return projects.findAllByOrderByCreatedAtDesc().stream()
                .filter(p -> qq.isEmpty() || (p.title + " " + nz(p.description)).toLowerCase().contains(qq))
                .filter(p -> sk.isEmpty() || nz(p.requiredSkills).toLowerCase().contains(sk))
                .filter(p -> st.isEmpty() || p.status.equals(st))
                .map(p -> svc.view(p, me)).toList();
    }

    @GetMapping("/mine")
    public List<ProjectView> mine(Authentication a) {
        User me = svc.me(a);
        return projects.findByOwnerOrderByCreatedAtDesc(me).stream().map(p -> svc.view(p, me)).toList();
    }

    @GetMapping("/teams")
    public List<ProjectView> teams(Authentication a) {
        User me = svc.me(a);
        List<Project> l = new ArrayList<>(projects.findByOwnerOrderByCreatedAtDesc(me));
        reqs.findByUserAndStatus(me, "ACCEPTED").forEach(r -> l.add(r.project));
        return l.stream().map(p -> svc.view(p, me)).toList();
    }

    @GetMapping("/{id}")
    public ProjectView get(Authentication a, @PathVariable Long id) { return svc.view(find(id), svc.me(a)); }

    @PostMapping
    public ProjectView create(Authentication a, @RequestBody ProjectReq r) {
        User me = svc.me(a);
        Project p = new Project();
        apply(p, r);
        p.owner = me;
        return svc.view(projects.save(p), me);
    }

    @PutMapping("/{id}")
    public ProjectView update(Authentication a, @PathVariable Long id, @RequestBody ProjectReq r) {
        User me = svc.me(a);
        Project p = find(id);
        if (!p.owner.id.equals(me.id)) throw Err.forbidden("Only the owner can edit this project");
        apply(p, r);
        return svc.view(projects.save(p), me);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(Authentication a, @PathVariable Long id) {
        User me = svc.me(a);
        Project p = find(id);
        if (!p.owner.id.equals(me.id)) throw Err.forbidden("Only the owner can delete this project");
        reqs.deleteByProject(p);
        projects.delete(p);
        return Map.of("deleted", true);
    }

    @PostMapping("/{id}/join")
    public ProjectView join(Authentication a, @PathVariable Long id, @RequestBody(required = false) JoinReq r) {
        User me = svc.me(a);
        Project p = find(id);
        if (p.owner.id.equals(me.id)) throw Err.bad("You own this project");
        if (!"OPEN".equals(p.status)) throw Err.bad("This project is not open for new members");
        if (svc.team(p).size() >= p.teamSize) throw Err.bad("The team is already full");
        if (reqs.findByProjectAndUser(p, me).isPresent()) throw Err.conflict("You have already requested to join this project");
        JoinRequest j = new JoinRequest();
        j.project = p;
        j.user = me;
        j.message = r == null ? null : r.message();
        reqs.save(j);
        return svc.view(p, me);
    }
}
