package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.RegisterRequest;
import com.nomnom.linguacore.dto.response.UserResponse;
import com.nomnom.linguacore.entity.User;
import com.nomnom.linguacore.exception.DuplicateResourceException;
import com.nomnom.linguacore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public UserResponse register(RegisterRequest request){
        //chan trung email
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email đã tồn tại: " + request.getEmail());
        }
        //2.Băm password ,ko luu thô
        String hash=passwordEncoder.encode(request.getPassword());
        //3.Create user
        User user = new User()
                .setEmail(request.getEmail())
                .setPasswordHash(hash)
                .setDisplayName(request.getDisplayName())
                .setRole("user");//gán role mặc định cho client là user
        User saved =userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getDisplayName(), saved.getRole());
    }
}
