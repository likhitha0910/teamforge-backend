package com.teamforge.controller;

import com.teamforge.dto.Dtos.ProfileReq;
import com.teamforge.entity.User;
import com.teamforge.exception.Err;
import com.teamforge.repository.UserRepository;
import com.teamforge.security.JwtService;
import com.teamforge.service.ProjectService;
import com.teamforge.service.Skills;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    private final ProjectService svc;
    private final JwtService jwt;

    public UserController(UserRepository users, ProjectService svc, JwtService jwt) {
        this.users = users; this.svc = svc; this.jwt = jwt;
    }

    @GetMapping("/profile")
    public User profile(Authentication a) { return svc.me(a); }

    @PutMapping("/profile")
    public Map<String, Object> update(Authentication a, @RequestBody ProfileReq r) {
        User u = svc.me(a);
        if (r.fullName() == null || r.fullName().isBlank()) throw Err.bad("Name cannot be empty");
        if (r.email() == null || !r.email().trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) throw Err.bad("Enter a valid email address");
        String email = r.email().trim().toLowerCase();
        if (!email.equals(u.email) && users.existsByEmail(email)) throw Err.conflict("This email is already registered");
        u.fullName = r.fullName().trim();
        u.email = email;
        u.department = r.department();
        u.year = r.year();
        u.skills = r.skills();
        u.bio = r.bio();
        users.save(u);
        // email is the token subject, so issue a fresh token
        return Map.of("token", jwt.generate(u.email), "user", u);
    }

    /** Find students by skill, e.g. /api/users/search?skill=java */
    @GetMapping("/search")
    public List<User> search(@RequestParam(defaultValue = "") String skill) {
        Set<String> want = Skills.parse(skill);
        return users.findAll().stream()
                .filter(u -> want.isEmpty() || Skills.parse(u.skills).stream().anyMatch(s -> want.stream().anyMatch(s::contains)))
                .toList();
    }
}
