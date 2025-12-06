package com.example.admin.service;


import com.example.admin.entity.User;
import com.example.admin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 技能1：用户注册
    public User register(String username, String password, String nickname) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在！");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setOnline(false);
        user.setLastActiveTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    // 技能2：用户登录
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                user.setOnline(true);
                user.setLastActiveTime(LocalDateTime.now());
                return userRepository.save(user);
            }
        }
        return null;
    }

    // 🆕 新增技能3：获取所有用户列表 (这就是报错缺少的方法)
    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
