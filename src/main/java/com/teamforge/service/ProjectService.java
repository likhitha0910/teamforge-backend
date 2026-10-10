package com.teamforge.service;

import com.teamforge.dto.Dtos.ProjectView;
import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import com.teamforge.exception.Err;
import com.teamforge.repository.JoinRequestRepository;
import com.teamforge.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {
    private final JoinRequestRepository reqs;
    private final UserRepository users;

    public ProjectService(JoinRequestRepository reqs, UserRepository users) { this.reqs = reqs; this.users = users; }

    public User me(Authentication a) {
        return users.findByEmail(a.getName()).orElseThrow(() -> Err.unauthorized("Please log in again"));
    }

    /** owner + accepted members */
    public List<User> team(Project p) {
        List<User> t = new ArrayList<>();
        t.add(p.owner);
        reqs.findByProjectAndStatus(p, "ACCEPTED").forEach(r -> t.add(r.user));
        return t;
    }

    public ProjectView view(Project p, User me) {
        List<User> t = team(p);
        String my = reqs.findByProjectAndUser(p, me).map(r -> r.status).orElse(null);
        return new ProjectView(p.id, p.title, p.description, p.requiredSkills, p.teamSize, p.status,
                p.owner.id, p.owner.fullName, t.size(), p.createdAt, t, my, p.owner.id.equals(me.id));
    }
}
