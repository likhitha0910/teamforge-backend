package com.teamforge.config;

import com.teamforge.entity.User;
import com.teamforge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the admin account (admin@teamforge.com / admin123) on first start. */
@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @Override
    public void run(String... args) {
        if (users.existsByEmail("admin@teamforge.com")) return;
        User u = new User();
        u.fullName = "Administrator";
        u.email = "admin@teamforge.com";
        u.password = encoder.encode("admin123");
        u.role = "ADMIN";
        users.save(u);
    }
}
