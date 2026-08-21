package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.LoginRequest;
import com.nomnom.linguacore.dto.request.RegisterRequest;
import com.nomnom.linguacore.dto.response.LoginResponse;
import com.nomnom.linguacore.dto.response.UserResponse;
import com.nomnom.linguacore.entity.User;
import com.nomnom.linguacore.exception.DuplicateResourceException;
import com.nomnom.linguacore.exception.UnauthorizedException;
import com.nomnom.linguacore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UnauthorizedException("Email hoặc mật khẩu không đúng"));
        if(!passwordEncoder.matches(request.getPassword() , user.getPasswordHash())){
            throw new UnauthorizedException("Email hoặc mật khẩu không đúng");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new LoginResponse(token,user.getEmail(),user.getDisplayName());
    }
}
