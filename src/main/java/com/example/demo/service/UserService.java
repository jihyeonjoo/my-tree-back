package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import java.util.List;

public interface UserService {

	UserResponse signUp(SignUpRequest request, String clientIp);

	LoginResponse login(LoginRequest request);

	List<UserResponse> findAll();

	UserResponse findByUserId(String userId);

	UserResponse update(String userId, UserUpdateRequest request);

	void delete(String userId);
}
