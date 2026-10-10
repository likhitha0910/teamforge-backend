package com.teamforge.controller;

import com.teamforge.dto.Dtos.*;
import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import com.teamforge.repository.JoinRequestRepository;
import com.teamforge.repository.ProjectRepository;
import com.teamforge.repository.UserRepository;
import com.teamforge.service.ProjectService;
import com.teamforge.service.Skills;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final ProjectRepository projects;
    private final UserRepository users;
    private final JoinRequestRepository reqs;
    private final ProjectService svc;

    public RecommendationController(ProjectRepository projects, UserRepository users, JoinRequestRepository reqs, ProjectService svc) {
        this.projects = projects; this.users = users; this.reqs = reqs; this.svc = svc;
    }

    @GetMapping
    public Map<String, Object> recommend(Authentication a) {
        User me = svc.me(a);
        List<ProjRec> recProjects = new ArrayList<>();
        List<StuRec> recStudents = new ArrayList<>();
        List<User> everyone = users.findAll();

        for (Project p : projects.findAll()) {
            if (p.owner.id.equals(me.id)) {
                // students whose skills match what MY project needs
                List<Long> teamIds = svc.team(p).stream().map(u -> u.id).toList();
                for (User u : everyone) {
                    if (teamIds.contains(u.id)) continue;
                    List<String> m = Skills.overlap(p.requiredSkills, u.skills);
                    if (!m.isEmpty()) recStudents.add(new StuRec(u, p.id, p.title, m.size(), m));
                }
            } else if ("OPEN".equals(p.status) && reqs.findByProjectAndUser(p, me).isEmpty()) {
                // projects that need MY skills
                List<String> m = Skills.overlap(p.requiredSkills, me.skills);
                if (!m.isEmpty()) recProjects.add(new ProjRec(svc.view(p, me), m.size(), m));
            }
        }
        recProjects.sort((x, y) -> y.score() - x.score());
        recStudents.sort((x, y) -> y.score() - x.score());
        return Map.of("projects", recProjects, "students", recStudents.stream().limit(30).toList());
    }
}
