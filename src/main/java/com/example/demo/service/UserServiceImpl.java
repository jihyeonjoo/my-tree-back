package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.repository.UserRepository;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private static final String LOOPBACK_V4 = "127.0.0.1";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserResponse signUp(SignUpRequest request, String clientIp) {
		if (userRepository.countByUserId(request.userId()) > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User ID already exists");
		}

		User user = new User(
			request.userId(),
			passwordEncoder.encode(request.password()),
			request.name(),
			LocalDateTime.now(),
			normalizeIpv4(clientIp)
		);

		userRepository.insert(user);
		return toUserResponse(user);
	}

	@Override
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByUserId(request.userId());
		if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID or password");
		}

		return new LoginResponse(user.getUserId(), user.getName(), LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return userRepository.findAll().stream()
			.map(this::toUserResponse)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse findByUserId(String userId) {
		return toUserResponse(getUserOrThrow(userId));
	}

	@Override
	public UserResponse update(String userId, UserUpdateRequest request) {
		User existingUser = getUserOrThrow(userId);
		existingUser.setPassword(passwordEncoder.encode(request.password()));
		existingUser.setName(request.name());
		userRepository.update(existingUser);
		return toUserResponse(existingUser);
	}

	@Override
	public void delete(String userId) {
		getUserOrThrow(userId);
		userRepository.deleteByUserId(userId);
	}

	private User getUserOrThrow(String userId) {
		User user = userRepository.findByUserId(userId);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		return user;
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(
			user.getUserId(),
			user.getName(),
			user.getCreatedAt(),
			user.getIpAddress()
		);
	}

	private String normalizeIpv4(String rawIp) {
		if (rawIp == null || rawIp.isBlank()) {
			return LOOPBACK_V4;
		}

		String candidate = rawIp.split(",")[0].trim();
		if ("::1".equals(candidate) || "0:0:0:0:0:0:0:1".equals(candidate)) {
			return LOOPBACK_V4;
		}
		if (candidate.startsWith("::ffff:")) {
			candidate = candidate.substring("::ffff:".length());
		}

		try {
			InetAddress address = InetAddress.getByName(candidate);
			if (address instanceof Inet4Address) {
				return address.getHostAddress();
			}
			if (address instanceof Inet6Address) {
				return LOOPBACK_V4;
			}
		}
		catch (UnknownHostException ignored) {
			return LOOPBACK_V4;
		}

		return LOOPBACK_V4;
	}
}
