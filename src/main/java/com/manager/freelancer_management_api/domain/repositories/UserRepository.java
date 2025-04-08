package com.manager.freelancer_management_api.domain.repositories;

import com.manager.freelancer_management_api.domain.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    UserDetails findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDocument(String document);
}
