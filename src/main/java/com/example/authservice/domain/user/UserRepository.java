package com.example.authservice.domain.user;

import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailAndPhoneNumber(String email, String phoneNumber);

    boolean existsByPhoneNumber(@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be a valid international format") String phoneNumber);
}
