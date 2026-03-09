package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
	@NotBlank(message = "userId is required")
	String userId,
	@NotBlank(message = "password is required")
	String password,
	@NotBlank(message = "name is required")
	String name
) {
}
