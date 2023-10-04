package com.ssmu.security.repository;

 
import org.springframework.data.jpa.repository.JpaRepository;

import com.ssmu.security.models.User;
 
public interface UserRepository extends JpaRepository<User, Long> {
 
}