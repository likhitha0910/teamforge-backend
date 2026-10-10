package com.teamforge.config;

import com.teamforge.entity.JoinRequest;
import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import com.teamforge.repository.JoinRequestRepository;
import com.teamforge.repository.ProjectRepository;
import com.teamforge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adds demo students and projects the first time the app starts with an empty projects table. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final ProjectRepository projects;
    private final JoinRequestRepository reqs;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, ProjectRepository projects, JoinRequestRepository reqs, PasswordEncoder encoder) {
        this.users = users; this.projects = projects; this.reqs = reqs; this.encoder = encoder;
    }

    private User user(String name, String email, String dept, int year, String skills, String bio) {
        return users.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.fullName = name; u.email = email; u.password = encoder.encode("demo1234");
            u.department = dept; u.year = year; u.skills = skills; u.bio = bio;
            return users.save(u);
        });
    }

    private Project project(User owner, String title, String desc, String skills, int size, String status) {
        Project p = new Project();
        p.owner = owner; p.title = title; p.description = desc; p.requiredSkills = skills; p.teamSize = size; p.status = status;
        return projects.save(p);
    }

    private void join(Project p, User u, String status, String msg) {
        JoinRequest j = new JoinRequest();
        j.project = p; j.user = u; j.status = status; j.message = msg;
        reqs.save(j);
    }

    @Override
    public void run(String... args) {
        if (projects.count() > 0) return;

        User aarav = user("Aarav Sharma", "aarav@demo.com", "Computer Science", 3, "Java, Spring Boot, MySQL, REST APIs", "Backend developer who loves clean APIs.");
        User priya = user("Priya Reddy", "priya@demo.com", "Information Technology", 2, "Python, Machine Learning, Data Analysis, TensorFlow", "Curious about AI and data.");
        User rohan = user("Rohan Mehta", "rohan@demo.com", "Computer Science", 4, "HTML, CSS, JavaScript, UI Design", "Frontend and design enthusiast.");
        User sneha = user("Sneha Iyer", "sneha@demo.com", "Electronics", 3, "Arduino, IoT, C++, Embedded Systems", "Building things that blink and beep.");
        User karthik = user("Karthik Nair", "karthik@demo.com", "Computer Science", 2, "Java, Android, Kotlin, Firebase", "Mobile app tinkerer.");

        Project p1 = project(aarav, "AI Based Attendance System", "An attendance system that marks students present using face recognition from a classroom camera.", "Java, Spring Boot, MySQL, Machine Learning", 4, "OPEN");
        project(priya, "Student Performance Predictor", "Predict exam results from attendance and assignment data, and show risk alerts to mentors.", "Python, Machine Learning, Data Analysis", 3, "OPEN");
        project(rohan, "Campus Event Portal", "A website where clubs post events and students RSVP, with a clean calendar view.", "HTML, CSS, JavaScript, Java", 4, "OPEN");
        project(sneha, "Smart Plant Watering System", "An IoT device that reads soil moisture and waters plants automatically, with a small dashboard.", "Arduino, IoT, C++, JavaScript", 3, "OPEN");
        project(karthik, "Campus Bus Tracker", "An Android app that shows live bus locations and arrival times for campus routes.", "Android, Kotlin, Java, MySQL", 5, "OPEN");
        project(aarav, "Online Study Group Finder", "Find and schedule study groups by subject, with chat links and reminders.", "Java, Spring Boot, JavaScript, MySQL", 4, "IN_PROGRESS");
        project(priya, "Resume Skill Analyzer", "Upload a resume and get a list of detected skills and suggested learning paths.", "Python, NLP, Machine Learning", 3, "COMPLETED");

        join(p1, priya, "ACCEPTED", "I have worked with face recognition models and can help with the ML part.");
        join(p1, rohan, "PENDING", "I can build the dashboard UI for this.");

        // give the first real (non-demo) user a couple of projects so their dashboard is not empty
        users.findAll().stream().filter(u -> !u.email.endsWith("@demo.com") && !"ADMIN".equals(u.role)).findFirst().ifPresent(me -> {
            Project m1 = project(me, "Project Collaboration Platform", "A platform where students post projects, list the skills they need and form teams.", "Java, Spring Boot, MySQL, JavaScript", 4, "OPEN");
            project(me, "Hackathon Team Matcher", "Match hackathon participants into balanced teams based on their skills and interests.", "Java, Spring Boot, REST APIs, JavaScript", 5, "OPEN");
            join(m1, aarav, "PENDING", "I can help with the Spring Boot backend.");
            join(m1, sneha, "PENDING", "Happy to help with testing and documentation.");
        });
    }
}
