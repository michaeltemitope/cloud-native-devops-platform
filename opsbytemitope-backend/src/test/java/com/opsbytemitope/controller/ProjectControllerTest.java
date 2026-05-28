package com.opsbytemitope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbytemitope.config.TestSecurityConfig;
import com.opsbytemitope.dto.request.CreateProjectRequest;
import com.opsbytemitope.dto.response.PagedResponse;
import com.opsbytemitope.dto.response.ProjectResponse;
import com.opsbytemitope.dto.response.UserResponse;
import com.opsbytemitope.entity.ProjectStatus;
import com.opsbytemitope.entity.Role;
import com.opsbytemitope.exception.ForbiddenException;
import com.opsbytemitope.exception.ResourceNotFoundException;
import com.opsbytemitope.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ProjectController MVC Tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    private ProjectResponse sampleProject;

    @BeforeEach
    void setUp() {
        UserResponse owner = UserResponse.builder()
                .id(1L)
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Johnson")
                .fullName("Alice Johnson")
                .role(Role.USER)
                .build();

        sampleProject = ProjectResponse.builder()
                .id(10L)
                .name("Website Redesign")
                .description("Redesign the company website")
                .status(ProjectStatus.ACTIVE)
                .dueDate(LocalDate.now().plusDays(60))
                .owner(owner)
                .totalTasks(5L)
                .completedTasks(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    @DisplayName("POST /projects – 201 with valid payload")
    void createProject_valid_returns201() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Website Redesign");
        request.setDescription("Redesign the company website");
        request.setDueDate(LocalDate.now().plusDays(60));

        when(projectService.createProject(any(), anyString())).thenReturn(sampleProject);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Website Redesign"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    @DisplayName("POST /projects – 400 when name is blank")
    void createProject_blankName_returns400() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    @DisplayName("GET /projects/{id} – 200 for project owner")
    void getProject_found_returns200() throws Exception {
        when(projectService.getProjectById(eq(10L), anyString())).thenReturn(sampleProject);

        mockMvc.perform(get("/api/v1/projects/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("Website Redesign"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    @DisplayName("GET /projects/{id} – 404 when project not found")
    void getProject_notFound_returns404() throws Exception {
        when(projectService.getProjectById(eq(999L), anyString()))
                .thenThrow(new ResourceNotFoundException("Project not found with id: 999"));

        mockMvc.perform(get("/api/v1/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "other@example.com")
    @DisplayName("GET /projects/{id} – 403 for non-owner")
    void getProject_nonOwner_returns403() throws Exception {
        when(projectService.getProjectById(eq(10L), anyString()))
                .thenThrow(new ForbiddenException("Not your project"));

        mockMvc.perform(get("/api/v1/projects/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    @DisplayName("GET /projects – 200 returns paged list")
    void listProjects_returns200() throws Exception {
        PagedResponse<ProjectResponse> paged = PagedResponse.<ProjectResponse>builder()
                .content(List.of(sampleProject))
                .page(0).size(20)
                .totalElements(1).totalPages(1)
                .first(true).last(true)
                .build();

        when(projectService.getMyProjects(anyString(), any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("Website Redesign"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    @DisplayName("DELETE /projects/{id} – 200 for project owner")
    void deleteProject_success_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
