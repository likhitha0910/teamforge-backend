package com.teamforge.controller;

import com.teamforge.dto.Dtos.*;
import com.teamforge.entity.User;
import com.teamforge.exception.Err;
import com.teamforge.repository.UserRepository;
import com.teamforge.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterReq r) {
        if (r.fullName() == null || r.fullName().isBlank()) throw Err.bad("Name cannot be empty");
        if (r.email() == null || !r.email().trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) throw Err.bad("Enter a valid email address");
        if (r.password() == null || r.password().isBlank()) throw Err.bad("Password cannot be empty");
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw Err.conflict("This email is already registered");
        User u = new User();
        u.fullName = r.fullName().trim();
        u.email = email;
        u.password = encoder.encode(r.password());
        u.department = r.department();
        u.year = r.year();
        u.skills = r.skills();
        u.bio = r.bio();
        users.save(u);
        return Map.of("token", jwt.generate(email), "user", u);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginReq r) {
        String email = r.email() == null ? "" : r.email().trim().toLowerCase();
        User u = users.findByEmail(email).orElseThrow(() -> Err.unauthorized("Invalid email or password"));
        if (r.password() == null || !encoder.matches(r.password(), u.password)) throw Err.unauthorized("Invalid email or password");
        return Map.of("token", jwt.generate(u.email), "user", u);
    }
}
