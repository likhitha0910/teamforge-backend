package com.teamforge.repository;

import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerOrderByCreatedAtDesc(User owner);
    List<Project> findAllByOrderByCreatedAtDesc();
}
