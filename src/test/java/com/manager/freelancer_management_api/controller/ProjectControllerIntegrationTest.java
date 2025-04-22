package com.manager.freelancer_management_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.manager.freelancer_management_api.config.AbstractIntegrationTest;
import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.request.CreateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.request.DeleteProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.request.UpdateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.user.dto.request.LoginRequestDTO;
import com.manager.freelancer_management_api.domain.user.dto.request.RegisterUserRequestDTO;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private String clientToken;
    private User clientUser;
    private String clientPassword = "clientPassword";

    private String freelancerToken;
    private User freelancerUser;
    private String freelancerPassword = "freelancerPassword";

    private final String BASE_URL = "/api/project";

    private String registerAndLogin(String fullName, String document, String email, String password, UserRole userRole, UserRole currentUserRole) throws Exception {
        RegisterUserRequestDTO registerReq = new RegisterUserRequestDTO(fullName, document, email, password, userRole, currentUserRole);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginReq = new LoginRequestDTO(email, password);
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        return JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");
    }
    private User getUserByEmail(String email) {
        return (User) userRepository.findByEmail(email);
    }
    private Project createProjectDirectly(String title, User owner) {
        Project project = Project.builder()
                .title(title)
                .description("Desc " + title)
                .deadline(new Deadline(LocalDate.now().plusDays(1), LocalDate.now().plusDays(30)))
                .estimatedBudget(new BigDecimal("1000"))
                .status(ProjectStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .user(owner)
                .build();
        return projectRepository.saveAndFlush(project);
    }

    @BeforeEach
    @Transactional
    void setup() throws Exception {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        clientToken = registerAndLogin("Client User", "11122233344", "client@test.com", clientPassword, UserRole.CLIENT, UserRole.CLIENT);
        clientUser = getUserByEmail("client@test.com");
        assertNotNull(clientUser, "Client user setup failed");

        freelancerToken = registerAndLogin("Freelancer User", "55566677788", "freelancer@test.com", freelancerPassword, UserRole.FREELANCER, UserRole.FREELANCER);
        freelancerUser = getUserByEmail("freelancer@test.com");
        assertNotNull(freelancerUser, "Freelancer user setup failed");
    }
    @Test
    @DisplayName("POST /create - Should create a Project with success to user CLIENT authenticated")
    void createProject_shouldReturnCreated_whenUserIsClient() throws Exception {
        CreateProjectRequestDTO request = new CreateProjectRequestDTO(
                "Projeto Teste API",
                "Descrição detalhada do projeto.",
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(32),
                new BigDecimal("5000.00")
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.message", is("Project created successfully")));

        assertTrue(projectRepository.findAll().stream()
                .anyMatch(p -> p.getTitle().equals("Projeto Teste API") && p.getUser().getId().equals(clientUser.getId())));
    }
    @Test
    @DisplayName("POST /create - Should return 403 Forbidden for user FREELANCER trying to create a project")
    void createProject_shouldReturnForbidden_whenUserIsFreelancer() throws Exception {
        CreateProjectRequestDTO request = new CreateProjectRequestDTO(
                "Projeto Freelancer",
                "Freelancer tentando criar",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10),
                new BigDecimal("100.00")
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /create - Should return 400 Bad Request for invalid DTO")
    void createProject_shouldReturnBadRequest_whenDtoIsInvalid() throws Exception {
        CreateProjectRequestDTO invalidRequest = new CreateProjectRequestDTO(
                null,
                "",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                BigDecimal.ZERO
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.errors", aMapWithSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.errors.description", containsString("cannot be blank")))
                .andExpect(jsonPath("$.errors.startDate", containsString("must be in the present or future")))
                .andExpect(jsonPath("$.errors.estimatedBudget", containsString("must be greater than zero")));
    }

    @Test
    @DisplayName("GET /projects - Should return list of paginade projects for authenticated user")
    void getProjects_shouldReturnPagedList_whenAuthenticated() throws Exception {
        createProjectDirectly("Projeto A", clientUser);
        createProjectDirectly("Projeto B", freelancerUser);

        mockMvc.perform(get(BASE_URL + "/projects")
                        .header("Authorization", "Bearer " + clientToken)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageable.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageable.pageSize", is(5)))
                .andExpect(jsonPath("$.sort.sorted", is(true)))
                .andExpect(jsonPath("$.content[0].title", is("Projeto A")))
                .andExpect(jsonPath("$.content[1].title", is("Projeto B")));
    }
    @Test
    @DisplayName("GET /projects - Should return 401 Unauthorized without token")
    void getProjects_shouldReturnUnauthorized_whenNoToken() throws Exception {
        mockMvc.perform(get(BASE_URL + "/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /projects/{id} - Should return project details for authenticated user")
    void getProjectById_shouldReturnProjectDetails_whenAuthenticated() throws Exception {
        Project project = createProjectDirectly("Projeto Detalhe", clientUser);

        mockMvc.perform(get(BASE_URL + "/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Projeto Detalhe")))
                .andExpect(jsonPath("$.description", is("Desc Projeto Detalhe")))
                .andExpect(jsonPath("$.projectOwnerProfile.fullName", is(clientUser.getFullName())));
    }
    @Test
    @DisplayName("GET /projects/{id} - Should return 404 Not Found for nonexistent Project id")
    void getProjectById_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        long nonExistentId = 9999L;
        mockMvc.perform(get(BASE_URL + "/projects/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)))
                .andExpect(jsonPath("$.message", is("Project not found.")));
    }
    @Test
    @DisplayName("GET /projects/{id} - Should return 401 Unauthorized without token")
    void getProjectById_shouldReturnUnauthorized_whenNoToken() throws Exception {
        Project project = createProjectDirectly("Projeto Detalhe No Token", clientUser);
        mockMvc.perform(get(BASE_URL + "/projects/{id}", project.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /projects/{id} - Should update project with success if user CLIENT is owner")
    void updateProject_shouldUpdateSuccessfully_whenClientIsOwner() throws Exception {
        Project project = createProjectDirectly("Projeto Original", clientUser);
        UpdateProjectRequestDTO updateRequest = new UpdateProjectRequestDTO(
                "Projeto Atualizado",
                "Nova descrição.",
                null,
                project.getDeadline().getEndDate().plusMonths(1),
                new BigDecimal("1500.50"),
                ProjectStatus.IN_PROGRESS
        );
        mockMvc.perform(put(BASE_URL + "/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.message", is("Project updated successfully")));

        Optional<Project> updatedProjectOpt = projectRepository.findById(project.getId());
        assertTrue(updatedProjectOpt.isPresent());
        Project updatedProject = updatedProjectOpt.get();
        assertEquals("Projeto Atualizado", updatedProject.getTitle());
        assertEquals("Nova descrição.", updatedProject.getDescription());
        assertEquals(0, new BigDecimal("1500.50").compareTo(updatedProject.getEstimatedBudget()));
        assertEquals(ProjectStatus.IN_PROGRESS, updatedProject.getStatus());
        assertEquals(project.getDeadline().getEndDate().plusMonths(1), updatedProject.getDeadline().getEndDate());
    }
    @Test
    @DisplayName("PUT /projects/{id} - Should return 403 Forbidden if user FREELANCER try to update")
    void updateProject_shouldReturnForbidden_whenUserIsFreelancer() throws Exception {
        Project project = createProjectDirectly("Projeto do Cliente", clientUser);
        UpdateProjectRequestDTO updateRequest = new UpdateProjectRequestDTO(
                "Tentativa Freelancer", null, null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/projects/{id}", project.getId())
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("PUT /projects/{id} - Should return 403 Forbidden if user CLIENT Try to update another CLIENT project")
    void updateProject_shouldReturnForbidden_whenClientUpdatesOthersProject() throws Exception {
        String otherClientToken = registerAndLogin("Other Client", "99988877766", "otherclient@test.com", "otherPass", UserRole.CLIENT, UserRole.CLIENT);
        User otherClientUser = getUserByEmail("otherclient@test.com");
        Project otherProject = createProjectDirectly("Projeto do Outro Cliente", otherClientUser);

        UpdateProjectRequestDTO updateRequest = new UpdateProjectRequestDTO(
                "Tentativa de Atualização Indevida", null, null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/projects/{id}", otherProject.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode", is(403)))
                .andExpect(jsonPath("$.message", is("Access denied.")));
    }
    @Test
    @DisplayName("PUT /projects/{id} - Should return 404 Not Found for nonexistent id")
    void updateProject_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        long nonExistentId = 9999L;
        UpdateProjectRequestDTO updateRequest = new UpdateProjectRequestDTO("Update Non Existent", null, null, null, null, null);

        mockMvc.perform(put(BASE_URL + "/projects/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)))
                .andExpect(jsonPath("$.message", is("Project not found.")));
    }

    @Test
    @DisplayName("DELETE /projects/{id} - Should delete project with success if CLIENT is owner and password is correct")
    void deleteProject_shouldDeleteSuccessfully_whenClientIsOwnerAndPasswordCorrect() throws Exception {
        Project project = createProjectDirectly("Projeto para Deletar", clientUser);
        long projectIdToDelete = project.getId();
        DeleteProjectRequestDTO deleteRequest = new DeleteProjectRequestDTO(clientPassword);

        mockMvc.perform(delete(BASE_URL + "/projects/{id}", projectIdToDelete)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.message", is("Project deleted successfully")));

        assertFalse(projectRepository.findById(projectIdToDelete).isPresent());
    }
    @Test
    @DisplayName("DELETE /projects/{id} - Should return 401 Unauthorized if the password is invalid")
    void deleteProject_shouldReturnUnauthorized_whenPasswordIsIncorrect() throws Exception {
        Project project = createProjectDirectly("Projeto Senha Errada", clientUser);
        long projectIdToDelete = project.getId();
        DeleteProjectRequestDTO deleteRequest = new DeleteProjectRequestDTO("wrongPassword");

        mockMvc.perform(delete(BASE_URL + "/projects/{id}", projectIdToDelete)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode", is(401)))
                .andExpect(jsonPath("$.message", is("Senha incorreta. Não foi possível excluir o projeto.")));

        assertTrue(projectRepository.findById(projectIdToDelete).isPresent());
    }
    @Test
    @DisplayName("DELETE /projects/{id} - Should return 400 Bad Request if password field is blank")
    void deleteProject_shouldReturnBadRequest_whenPasswordInDtoIsMissing() throws Exception {
        Project project = createProjectDirectly("Projeto Senha Faltando", clientUser);
        long projectIdToDelete = project.getId();
        DeleteProjectRequestDTO deleteRequest = new DeleteProjectRequestDTO(null);

        mockMvc.perform(delete(BASE_URL + "/projects/{id}", projectIdToDelete)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));

        assertTrue(projectRepository.findById(projectIdToDelete).isPresent());
    }
    @Test
    @DisplayName("DELETE /projects/{id} - Should return 403 Forbidden if FREELANCER try to delete a project")
    void deleteProject_shouldReturnForbidden_whenUserIsFreelancer() throws Exception {
        Project project = createProjectDirectly("Projeto Protegido", clientUser);
        long projectIdToDelete = project.getId();
        DeleteProjectRequestDTO deleteRequest = new DeleteProjectRequestDTO(clientPassword);

        mockMvc.perform(delete(BASE_URL + "/projects/{id}", projectIdToDelete)
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName("DELETE /projects/{id} - Should return 403 Forbidden if CLIENT try to delete another user project")
    void deleteProject_shouldReturnForbidden_whenClientDeletesOthersProject() throws Exception {
        String otherClientToken = registerAndLogin("Other Client Del", "77766655544", "otherclientdel@test.com", "otherPassDel", UserRole.CLIENT, UserRole.CLIENT);
        User otherClientUser = getUserByEmail("otherclientdel@test.com");
        Project otherProject = createProjectDirectly("Projeto do Outro Cliente Del", otherClientUser);
        DeleteProjectRequestDTO deleteRequest = new DeleteProjectRequestDTO(clientPassword);

        mockMvc.perform(delete(BASE_URL + "/projects/{id}", otherProject.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode", is(403)))
                .andExpect(jsonPath("$.message", is("Access denied.")));

        assertTrue(projectRepository.findById(otherProject.getId()).isPresent());
    }
    @Test
    @DisplayName("DELETE /projects/{id} - Should return 404 Not Found for nonexistent id")
    void deleteProject_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        long nonExistentId = 9999L;
        DeleteProjectRequestDTO deleteRequest = new DeleteProjectRequestDTO(clientPassword);

        mockMvc.perform(delete(BASE_URL + "/projects/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(404)))
                .andExpect(jsonPath("$.message", is("Project not found.")));
    }
}