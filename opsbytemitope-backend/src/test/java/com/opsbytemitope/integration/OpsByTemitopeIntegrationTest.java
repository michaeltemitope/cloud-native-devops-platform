package com.opsbytemitope.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbytemitope.dto.request.CreateProjectRequest;
import com.opsbytemitope.dto.request.LoginRequest;
import com.opsbytemitope.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration test using a real PostgreSQL database via Testcontainers.
 * Redis caching is disabled for these tests (cache.type=none in test profile).
 * Tests are ordered to share state across the registration → login → CRUD flow.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("OpsByTemitope Integration Tests")
class OpsByTemitopeIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("opsbytemitope_it")
            .withUsername("opsbytemitope")
            .withPassword("opsbytemitope_test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Shared state across ordered tests
    private static String jwtToken;
    private static Long projectId;

    @BeforeEach
    void ensureContainerRunning() {
        // Testcontainers @Container lifecycle handles this; just a guard
        assert postgres.isRunning();
    }

    // ── Registration ─────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("1. POST /auth/register – creates account and returns JWT")
    void register_createsUserAndReturnsToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Integration");
        request.setLastName("Tester");
        request.setEmail("it@opsbytemitope.com");
        request.setPassword("IntegrationTest1!");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(body)
                .path("data").path("accessToken").asText();
    }

    // ── Login ────────────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("2. POST /auth/login – authenticates and returns fresh JWT")
    void login_authenticatesAndReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("it@opsbytemitope.com");
        request.setPassword("IntegrationTest1!");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(body)
                .path("data").path("accessToken").asText();
    }

    // ── Profile ──────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("3. GET /users/me – returns authenticated user profile")
    void getUserProfile_returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("it@opsbytemitope.com"))
                .andExpect(jsonPath("$.data.firstName").value("Integration"));
    }

    // ── Projects ─────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("4. POST /projects – creates a project")
    void createProject_returnsCreated() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("IT Test Project");
        request.setDescription("Created during integration test run");
        request.setDueDate(LocalDate.now().plusDays(30));

        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("IT Test Project"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        projectId = objectMapper.readTree(body)
                .path("data").path("id").asLong();
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /projects – lists user projects")
    void listProjects_returnsAtLeastOne() throws Exception {
        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @Order(6)
    @DisplayName("6. GET /projects/{id} – fetches project by ID")
    void getProjectById_returnsProject() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + projectId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(projectId))
                .andExpect(jsonPath("$.data.name").value("IT Test Project"));
    }

    // ── Dashboard ────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("7. GET /dashboard – returns summary metrics")
    void getDashboard_returnsSummary() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProjects").value(1))
                .andExpect(jsonPath("$.data.tasksByStatus").isMap());
    }

    // ── Actuator ─────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("8. GET /actuator/health – returns UP without auth")
    void actuatorHealth_returnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @Order(9)
    @DisplayName("9. Unauthenticated request to protected endpoint returns 403")
    void protectedEndpoint_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isForbidden());
    }
}
