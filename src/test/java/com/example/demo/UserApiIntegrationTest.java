package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@Sql(statements = "DELETE FROM users", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserApiIntegrationTest {

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void signUpAndLoginShouldPersistUser() throws Exception {
		SignUpRequest signUpRequest = new SignUpRequest("hong01", "pass1234", "Hong");

		mockMvc.perform(post("/api/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-For", "192.168.0.10")
				.content(objectMapper.writeValueAsString(signUpRequest)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.userId").value("hong01"))
			.andExpect(jsonPath("$.name").value("Hong"))
			.andExpect(jsonPath("$.ipAddress").value("192.168.0.10"));

		if (userRepository.findByUserId("hong01") == null) {
			throw new AssertionError("User was not inserted into the database");
		}

		LoginRequest loginRequest = new LoginRequest("hong01", "pass1234");

		mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value("hong01"))
			.andExpect(jsonPath("$.name").value("Hong"));
	}

	@Test
	void userCrudEndpointsShouldWork() throws Exception {
		mockMvc.perform(post("/api/users/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-For", "10.0.0.25")
				.content(objectMapper.writeValueAsString(new SignUpRequest("kim01", "pw123456", "Kim"))))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/users/kim01"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value("kim01"))
			.andExpect(jsonPath("$.name").value("Kim"));

		mockMvc.perform(put("/api/users/kim01")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new UserUpdateRequest("pw999999", "UpdatedKim"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("UpdatedKim"));

		mockMvc.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("kim01", "pw999999"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value("kim01"));

		mockMvc.perform(get("/api/users"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].userId").value("kim01"));

		mockMvc.perform(delete("/api/users/kim01"))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/users/kim01"))
			.andExpect(status().isNotFound());
	}
}
