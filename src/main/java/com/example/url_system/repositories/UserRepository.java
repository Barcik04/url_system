package com.example.url_system.repositories;

import com.example.url_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String s);
    Optional<User> findByUsername(String s);
}
