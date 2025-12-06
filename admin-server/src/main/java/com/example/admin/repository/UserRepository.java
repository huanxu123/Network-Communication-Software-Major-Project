package com.example.admin.repository;

import com.example.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 找人：根据用户名查用户
    Optional<User> findByUsername(String username);

    // 查重：看看用户名是不是被注册过了
    boolean existsByUsername(String username);
}
