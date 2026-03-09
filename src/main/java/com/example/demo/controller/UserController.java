package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/signup")
	public ResponseEntity<UserResponse> signUp(
		@Valid @RequestBody SignUpRequest request,
		@RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
		HttpServletRequest httpServletRequest
	) {
		String clientIp = forwardedFor != null ? forwardedFor : httpServletRequest.getRemoteAddr();
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(userService.signUp(request, clientIp));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(userService.login(request));
	}

	@GetMapping
	public ResponseEntity<List<UserResponse>> findAll() {
		return ResponseEntity.ok(userService.findAll());
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> findByUserId(@PathVariable String userId) {
		return ResponseEntity.ok(userService.findByUserId(userId));
	}

	@PutMapping("/{userId}")
	public ResponseEntity<UserResponse> update(
		@PathVariable String userId,
		@Valid @RequestBody UserUpdateRequest request
	) {
		return ResponseEntity.ok(userService.update(userId, request));
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> delete(@PathVariable String userId) {
		userService.delete(userId);
		return ResponseEntity.noContent().build();
	}
}
