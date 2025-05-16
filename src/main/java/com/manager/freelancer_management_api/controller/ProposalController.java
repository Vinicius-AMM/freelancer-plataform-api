package com.manager.freelancer_management_api.controller;

import com.manager.freelancer_management_api.domain.global.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.global.dto.DTOValidationErrorResponse;
import com.manager.freelancer_management_api.domain.proposal.dto.request.CreateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.DeleteProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.ProcessProposalDecisionRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.request.UpdateProposalRequestDTO;
import com.manager.freelancer_management_api.domain.proposal.dto.response.ProposalResponseDTO;
import com.manager.freelancer_management_api.domain.proposal.service.IProposalService;
import com.manager.freelancer_management_api.infra.security.SecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.buildSuccessResponse;

@RestController
@RequestMapping("/api/proposal")
@SecurityRequirement(name = SecurityConfig.SECURITY)
@Slf4j
public class ProposalController {
    private final IProposalService proposalService;

    public ProposalController(IProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping("/project/{projectId}/create")
    @Operation(summary = "Cria uma nova proposta para um projeto", description = "Permite que um freelancer autenticado envie uma proposta para um projeto específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proposal created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 201, \"message\": \"Proposal created successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (DTO validation failed or invalid dates)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": {\"startDate\": \"Start date cannot be null.\", \"endDate\": \"End date must be after start date.\", \"offeredValue\": \"must be greater than 0\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (not authenticated or token invalid)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not a FREELANCER or trying to propose to own project)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 403, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Project or Freelancer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"Project not found.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ApiResponseDTO> createProposal(@PathVariable Long projectId, @RequestBody @Valid CreateProposalRequestDTO proposalData) {
        log.info("Received request to create proposal for project ID: {}", projectId);
        log.debug("Proposal creation DTO: {}", proposalData);
        proposalService.createProposal(projectId, proposalData);
        log.info("Proposal created successfully for project ID: {}", projectId);
        return buildSuccessResponse(HttpStatus.CREATED, "Proposal created successfully");
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all proposals for a specific project", description = "Allows an authenticated CLIENT (owner of the project) to view all proposals for their project, with pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposals retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProposalResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not the project owner or not a CLIENT)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<List<ProposalResponseDTO>> getProposalsByProjectId(@PathVariable Long projectId, @ParameterObject Pageable pageable) {
        log.info("Received request to get proposals for project ID: {}, pageable: {}", projectId, pageable);
        Page<ProposalResponseDTO> proposals = proposalService.getAllProposalsByProjectId(projectId, pageable);
        log.info("Retrieved {} proposals for project ID: {}", proposals.getTotalElements(), projectId);
        return ResponseEntity.ok(proposals.getContent());
    }

    @GetMapping("/freelancer/{freelancerId}")
    @Operation(summary = "Get all proposals submitted by a specific freelancer", description = "Allows an authenticated FREELANCER to view their own submitted proposals, with pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposals retrieved successfully",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProposalResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not the specified freelancer or not a FREELANCER)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Freelancer (User) not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<List<ProposalResponseDTO>> getProposalsByFreelancerId(@PathVariable UUID freelancerId, @ParameterObject Pageable pageable) {
        log.info("Received request to get proposals for freelancer ID: {}, pageable: {}", freelancerId, pageable);
        Page<ProposalResponseDTO> proposals = proposalService.getAllProposalsByFreelancerId(freelancerId, pageable);
        log.info("Retrieved {} proposals for freelancer ID: {}", proposals.getTotalElements(), freelancerId);
        return ResponseEntity.ok(proposals.getContent());
    }

    @GetMapping("/proposal/{proposalId}")
    @Operation(summary = "Get a specific proposal by its ID", description = "Allows authenticated users (CLIENT or FREELANCER involved) to view a specific proposal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProposalResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not related to the proposal)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"Proposal not found.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ProposalResponseDTO> getProposalById(@PathVariable Long proposalId) {
        log.info("Received request to get proposal by ID: {}", proposalId);
        ProposalResponseDTO proposal = proposalService.getProposalById(proposalId);
        log.info("Proposal ID: {} retrieved successfully.", proposalId);
        return ResponseEntity.ok(proposal);
    }

    @PutMapping("/{proposalId}/update")
    @Operation(summary = "Update an existing proposal", description = "Allows the FREELANCER who created the proposal to update its details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Proposal updated successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (DTO validation or invalid dates)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not the owner of the proposal or not a FREELANCER)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO> updateProposal(@PathVariable Long proposalId, @RequestBody @Valid UpdateProposalRequestDTO proposalData) {
        log.info("Received request to update proposal ID: {}", proposalId);
        log.debug("Proposal update DTO: {}", proposalData);
        proposalService.updateProposal(proposalId, proposalData);
        log.info("Proposal ID: {} updated successfully.", proposalId);
        return buildSuccessResponse(HttpStatus.OK, "Proposal updated successfully");
    }

    @DeleteMapping("/{proposalId}/delete")
    @Operation(summary = "Delete a proposal", description = "Allows the FREELANCER who created the proposal to delete it, requires password confirmation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal deleted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Proposal deleted successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (password missing)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (password incorrect)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not the owner of the proposal or not a FREELANCER)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ResponseEntity<ApiResponseDTO> deleteProposal(@PathVariable Long proposalId, @RequestBody @Valid DeleteProposalRequestDTO deleteRequest) {
        log.info("Received request to delete proposal ID: {}", proposalId);
        proposalService.deleteProposal(proposalId, deleteRequest.rawPassword());
        log.info("Proposal ID: {} deleted successfully.", proposalId);
        return buildSuccessResponse(HttpStatus.OK, "Proposal deleted successfully");
    }

    @PostMapping("/{proposalId}/decision")
    @Operation(summary = "Process a decision on a proposal (Accept/Decline)", description = "Allows the CLIENT who owns the project to accept or decline a proposal. Requires password confirmation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal decision processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 200, \"message\": \"Proposal decision processed successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request data (action or password missing, or project not available for decision)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (password incorrect)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not the project owner or not a CLIENT)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Proposal or Project not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g., project status not OPEN/NEGOTIATING)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 409, \"message\": \"Proposals can only be accepted or rejected for projects that are OPEN or NEGOTIATING. Current status: IN_PROGRESS\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ApiResponseDTO> processProposalDecision(@PathVariable Long proposalId, @RequestBody @Valid ProcessProposalDecisionRequestDTO decisionRequest) {
        log.info("Received request to process decision for proposal ID: {}. Action: {}", proposalId, decisionRequest.action());
        log.debug("Proposal decision DTO: {}", decisionRequest);
        proposalService.processProposalDecision(proposalId, decisionRequest);
        log.info("Decision for proposal ID: {} (Action: {}) processed successfully.", proposalId, decisionRequest.action());
        return buildSuccessResponse(HttpStatus.OK, "Proposal decision processed successfully");
    }
}
