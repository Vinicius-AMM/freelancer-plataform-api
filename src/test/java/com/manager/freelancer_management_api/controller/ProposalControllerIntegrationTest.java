package com.manager.freelancer_management_api.controller;

import com.jayway.jsonpath.JsonPath;
import com.manager.freelancer_management_api.config.AbstractIntegrationTest;
import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.repositories.ProjectRepository;
import com.manager.freelancer_management_api.domain.proposal.dto.request.CreateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.UpdateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.domain.proposal.repository.ProposalRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProposalControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    private String clientToken;
    private User clientUser;
    private final String clientPassword = "clientPassword";

    private String otherClientToken;
    private User otherClientUser;
    private final String otherClientPassword = "clientPassword";

    private String freelancerToken;
    private User freelancerUser;
    private final String freelancerPassword = "freelancerPassword";

    private String otherFreelancerToken;
    private User otherFreelancerUser;
    private final String otherFreelancerPassword = "otherFreelancerPass";


    private Project projectOwnedByClient;
    private Project otherClientsProject;
    private Project projectForProposals;

    private final String BASE_PROPOSAL_URL = "/api/proposal";
    private final String BASE_PROJECT_URL = "/api/project";
    private final String AUTH_URL = "/auth";

    private String registerAndLogin(String fullName, String document, String email, String password, UserRole mainRole, UserRole currentRole) throws Exception {
        RegisterUserRequestDTO registerReq = new RegisterUserRequestDTO(fullName, document, email, password, mainRole, currentRole);
        mockMvc.perform(post(AUTH_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginReq = new LoginRequestDTO(email, password);
        MvcResult loginResult = mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");
    }

    private Project createProjectDirectlyInDb(String title, User owner, ProjectStatus status) {
        Project project = Project.builder()
                .title(title)
                .description("Desc for " + title)
                .deadline(new Deadline(LocalDate.now().plusDays(1), LocalDate.now().plusDays(30)))
                .estimatedBudget(BigDecimal.valueOf(1000))
                .status(status)
                .createdAt(LocalDateTime.now())
                .user(owner)
                .build();
        return projectRepository.saveAndFlush(project);
    }

    private Proposal createProposalDirectlyInDb(Project project, User freelancer, BigDecimal value) {
        Proposal proposal = Proposal.builder()
                .project(project)
                .freelancer(freelancer)
                .deadline(new Deadline(LocalDate.now().plusDays(2), LocalDate.now().plusDays(20)))
                .offeredValue(value)
                .createdAt(LocalDateTime.now())
                .build();
        return proposalRepository.saveAndFlush(proposal);
    }

    @BeforeEach
    @Transactional
    public void setUp() throws Exception {
        proposalRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();

        clientToken = registerAndLogin("Client User", "11122233344", "client@test.com", clientPassword, UserRole.CLIENT, UserRole.CLIENT);
        clientUser = (User) userRepository.findByEmail("client@test.com");
        assertNotNull(clientUser, "Client user setup failed");

        otherClientToken = registerAndLogin("Other Client", "22233344455", "other.client@test.com", otherClientPassword, UserRole.CLIENT, UserRole.CLIENT);
        otherClientUser = (User) userRepository.findByEmail("other.client@test.com");
        assertNotNull(otherClientUser, "Other client user setup failed");

        freelancerToken = registerAndLogin("Test Freelancer User", "44455566601", "freelancer.proposal@example.com", freelancerPassword, UserRole.FREELANCER, UserRole.FREELANCER);
        freelancerUser = (User) userRepository.findByEmail("freelancer.proposal@example.com");
        assertNotNull(freelancerUser, "Freelancer user não foi criado.");

        otherFreelancerToken = registerAndLogin("Other Freelancer User", "77788899901", "other.freelancer@example.com", otherFreelancerPassword, UserRole.FREELANCER, UserRole.FREELANCER);
        otherFreelancerUser = (User) userRepository.findByEmail("other.freelancer@example.com");
        assertNotNull(otherFreelancerUser, "Other freelancer user não foi criado.");

        projectForProposals = createProjectDirectlyInDb("Project for proposals", clientUser, ProjectStatus.OPEN);

        otherClientsProject = createProjectDirectlyInDb("other client project", otherClientUser, ProjectStatus.OPEN);
    }
    
    @Test
    @Transactional
    @DisplayName("POST /project/{projectId}/create - Should create a proposal for a project")
    void createProposal_asFreelancer_shouldSucceed() throws Exception {
        CreateProposalRequestDTO proposalDto = new CreateProposalRequestDTO(
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(27),
                new BigDecimal("1800.00")
        );

        mockMvc.perform(post(BASE_PROPOSAL_URL + "/project/{projectId}/create", projectForProposals.getId())
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proposalDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.message", is("Proposal created successfully")));

        assertEquals(1, proposalRepository.count());
        Optional<Project> updatedProject = projectRepository.findById(projectForProposals.getId());
        assertTrue(updatedProject.isPresent());
        assertEquals(ProjectStatus.NEGOTIATING, updatedProject.get().getStatus());
    }

    @Test
    @Transactional
    @DisplayName("POST /project/{projectId}/create - Should fail if user is not freelancer")
    void createProposal_asClient_shouldFail() throws Exception {
        CreateProposalRequestDTO proposalDto = new CreateProposalRequestDTO(
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(27),
                new BigDecimal("1800.00")
        );

        mockMvc.perform(post(BASE_PROPOSAL_URL + "/project/{projectId}/create", projectForProposals.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proposalDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /project/{projectId}/create - Error: Invalid projectId")
    void createProposal_error_invalidProjectId() throws Exception {
        CreateProposalRequestDTO dto = new CreateProposalRequestDTO(LocalDate.now().plusDays(5), LocalDate.now().plusDays(25), BigDecimal.valueOf(1500));
        mockMvc.perform(post(BASE_PROPOSAL_URL + "/project/9999/create")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Project not found.")));
    }

    @Test
    @DisplayName("POST /project/{projectId}/create - Error: Invalid DTO (start date null)")
    void createProposal_error_invalidDto_nullStartDate() throws Exception {
        CreateProposalRequestDTO dto = new CreateProposalRequestDTO(null, LocalDate.now().plusDays(25), BigDecimal.valueOf(1500));
        mockMvc.perform(post(BASE_PROPOSAL_URL + "/project/{projectId}/create", projectForProposals.getId())
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Start date and end date cannot be null.")));
    }

    @Test
    @DisplayName("POST /project/{projectId}/create - Error: Project not OPEN or NEGOTIATING")
    @Transactional
    void createProposal_error_projectNotInOpenOrNegotiating() throws Exception {
        CreateProposalRequestDTO request = new CreateProposalRequestDTO(LocalDate.now().plusDays(5), LocalDate.now().plusDays(25), BigDecimal.valueOf(1500));
        Project projectInProgress = createProjectDirectlyInDb("Project In Progress", clientUser, ProjectStatus.IN_PROGRESS);
        mockMvc.perform(post(BASE_PROPOSAL_URL + "/project/{projectId}/create", projectInProgress.getId())
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Proposals can only be created for projects that are OPEN or NEGOTIATING. Current status: " + projectInProgress.getStatus())));
    }

    @Test
    @DisplayName("GET /project/{projectId} - Success: Client (owner) gets proposals")
    void getProposalsByProjectId_success_clientOwner() throws Exception {
        createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1200));
        createProposalDirectlyInDb(projectForProposals, otherFreelancerUser, BigDecimal.valueOf(1300));

        mockMvc.perform(get(BASE_PROPOSAL_URL + "/project/{projectId}", projectForProposals.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /project/{projectId} - Error: Freelancer tries to get")
    void getProposalsByProjectId_error_freelancerTries() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/project/{projectId}", projectForProposals.getId())
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /project/{projectId} - Error: Client tries for another client's project")
    void getProposalsByProjectId_error_clientForOtherClientProject() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/project/{projectId}", otherClientsProject.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /project/{projectId} - Error: Invalid projectId")
    void getProposalsByProjectId_error_invalidProjectId() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/project/9999")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /freelancer/{freelancerId} - Success: Freelancer gets own proposals")
    void getProposalsByFreelancerId_success_ownProposals() throws Exception {
        createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        createProposalDirectlyInDb(otherClientsProject, freelancerUser, BigDecimal.valueOf(1100));

        mockMvc.perform(get(BASE_PROPOSAL_URL + "/freelancer/{freelancerId}", freelancerUser.getId())
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /freelancer/{freelancerId} - Error: Client tries to get")
    void getProposalsByFreelancerId_error_clientTries() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/freelancer/{freelancerId}", freelancerUser.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /freelancer/{freelancerId} - Error: Freelancer for another's proposals")
    void getProposalsByFreelancerId_error_freelancerForAnother() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/freelancer/{freelancerId}", otherFreelancerUser.getId())
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /freelancer/{freelancerId} - Error: Unauthenticated")
    void getProposalsByFreelancerId_error_unauthenticated() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/freelancer/{freelancerId}", freelancerUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /freelancer/{freelancerId} - Error: Invalid freelancerId")
    void getProposalsByFreelancerId_error_invalidFreelancerId() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/freelancer/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /proposal/{proposalId} - Success: Client (project owner) gets proposal")
    void getProposalById_success_clientOwner() throws Exception {
        Proposal p = createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/proposal/{proposalId}", p.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(p.getId().intValue())));
    }

    @Test
    @DisplayName("GET /proposal/{proposalId} - Success: Freelancer (proposal owner) gets proposal")
    void getProposalById_success_freelancerOwner() throws Exception {
        Proposal p = createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/proposal/{proposalId}", p.getId())
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(p.getId().intValue())));
    }

    @Test
    @DisplayName("GET /proposal/{proposalId} - Error: Unrelated user tries")
    void getProposalById_error_unrelatedUser() throws Exception {
        Proposal p = createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/proposal/{proposalId}", p.getId())
                        .header("Authorization", "Bearer " + otherFreelancerToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("GET /proposal/{proposalId} - Error: Invalid proposalId")
    void getProposalById_error_invalidProposalId() throws Exception {
        mockMvc.perform(get(BASE_PROPOSAL_URL + "/proposal/9999")
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{proposalId}/update - Success: Freelancer (owner) updates")
    void updateProposal_success_freelancerOwner() throws Exception {
        Proposal p = createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        UpdateProposalRequestDTO dto = new UpdateProposalRequestDTO(null, null, BigDecimal.valueOf(1200));
        mockMvc.perform(put(BASE_PROPOSAL_URL + "/{proposalId}/update", p.getId())
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Proposal updated successfully")));
        Proposal updated = proposalRepository.findById(p.getId()).orElseThrow();
        assertEquals(0, BigDecimal.valueOf(1200).compareTo(updated.getOfferedValue()));
    }

    @Test
    @DisplayName("PUT /{proposalId}/update - Error: Client tries to update")
    void updateProposal_error_clientTries() throws Exception {
        Proposal p = createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        UpdateProposalRequestDTO dto = new UpdateProposalRequestDTO(null, null, BigDecimal.valueOf(1200));
        mockMvc.perform(put(BASE_PROPOSAL_URL + "/{proposalId}/update", p.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /{proposalId}/update - Error: Freelancer updates another's proposal")
    void updateProposal_error_freelancerUpdatesAnother() throws Exception {
        Proposal p = createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        UpdateProposalRequestDTO dto = new UpdateProposalRequestDTO(null, null, BigDecimal.valueOf(1200));
        mockMvc.perform(put(BASE_PROPOSAL_URL + "/{proposalId}/update", p.getId())
                        .header("Authorization", "Bearer " + otherFreelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /{proposalId}/update - Error: Invalid DTO (dates)")
    void updateProposal_error_invalidDtoDates() throws Exception {
        Proposal p = createProposalDirectlyInDb(projectForProposals, freelancerUser, BigDecimal.valueOf(1000));
        UpdateProposalRequestDTO dto = new UpdateProposalRequestDTO(LocalDate.now().plusDays(10), LocalDate.now().plusDays(5), null);
        mockMvc.perform(put(BASE_PROPOSAL_URL + "/{proposalId}/update", p.getId())
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("The final date must be after the start date.")));
    }
}
