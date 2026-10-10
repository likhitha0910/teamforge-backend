package com.teamforge.repository;

import com.teamforge.entity.JoinRequest;
import com.teamforge.entity.Project;
import com.teamforge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByProjectAndStatus(Project project, String status);
    List<JoinRequest> findByUserOrderByCreatedAtDesc(User user);
    List<JoinRequest> findByUserAndStatus(User user, String status);
    List<JoinRequest> findByProjectOwnerOrderByCreatedAtDesc(User owner);
    Optional<JoinRequest> findByProjectAndUser(Project project, User user);
    @Transactional
    void deleteByProject(Project project);
    @Transactional
    void deleteByUser(User user);
}
