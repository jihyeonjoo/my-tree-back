package com.example.demo.dto;

import java.time.LocalDateTime;

public record LoginResponse(
	String userId,
	String name,
	LocalDateTime loginAt
) {
}
