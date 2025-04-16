package com.manager.freelancer_management_api.controller;

import com.manager.freelancer_management_api.domain.global.dto.ApiResponseDTO;
import com.manager.freelancer_management_api.domain.global.dto.DTOValidationErrorResponse;
import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.dto.request.CreateProjectRequestDTO;
import com.manager.freelancer_management_api.domain.project.dto.response.ProjectResponseDTO;
import com.manager.freelancer_management_api.domain.project.enums.ProjectStatus;
import com.manager.freelancer_management_api.domain.project.service.IProjectService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;

import static com.manager.freelancer_management_api.utils.handler.ApiResponseUtil.buildSuccessResponse;

@RestController
@RequestMapping("/api/project")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class ProjectController {

    private final IProjectService projectService;

    public ProjectController(IProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create")
    @Operation(summary = "Cria um novo projeto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Novo projeto criado com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponseDTO.class),
                    examples = @ExampleObject(value = "{\"statusCode\": 201, \"message\": \"Project created successfully\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
            )),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação do DTO)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DTOValidationErrorResponse.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"errors\": { \"title\": \"Title cannot exceed 150 characters.\", \"description\": \"Description cannot be blank.\", \"startDate\": \"Start date cannot be null.\", \"endDate\": \"End date cannot be null.\", \"estimatedBudget\": \"Estimated Budget must be greater than zero.\"}, \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 400, \"message\": \"User not found\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")))
    })
    public ResponseEntity<ApiResponseDTO> createProject(@RequestBody @Valid CreateProjectRequestDTO projectData) {
        projectService.createProject(projectData);
        return buildSuccessResponse(HttpStatus.CREATED, "Project created successfully");
    }

    @GetMapping("/projects")
    @Operation(summary = "Retorna os projetos com limite de 10 por página")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projetos exibidos com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProjectResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    ))
    })
    public ResponseEntity<Page<ProjectResponseDTO>> getProjects(@ParameterObject Pageable pageable) {
        Page<ProjectResponseDTO> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/projects/{id}")
    @Operation(summary = "Retorna um projeto pelo id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto exibido com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"statusCode\": 401, \"message\": \"Access denied.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    )),
            @ApiResponse(responseCode = "404", description = "Projeto nao encontrado",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponseDTO.class),
                        examples = @ExampleObject(value = "{\"statusCode\": 404, \"message\": \"Project not found.\", \"timestamp\": \"2025-04-02T02:28:59.409Z\"}")
                    ))
    })
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @PatchMapping("/projects/{id}/updateTitle")
    @Operation(summary = "Atualiza o título de um projeto")
    public ResponseEntity<ApiResponseDTO> updateProjectTitle(@PathVariable Long id, @RequestBody String title) {
        projectService.updateTitle(id, title);
        return buildSuccessResponse(HttpStatus.OK, "Project title updated successfully");
    }

    @PatchMapping("/projects/{id}/updateDescription")
    @Operation(summary = "Atualiza a descrição de um projeto")
    public ResponseEntity<ApiResponseDTO> updateProjectDescription(@PathVariable Long id, @RequestBody String description) {
        projectService.updateDescription(id, description);
        return buildSuccessResponse(HttpStatus.OK, "Project description updated successfully");
    }

    @PatchMapping("/projects/{id}/updateDeadline")
    @Operation(summary = "Atualiza a data de entrega de um projeto")
    public ResponseEntity<ApiResponseDTO> updateProjectDeadline(@PathVariable Long id, @RequestBody Deadline deadline) {
        projectService.updateDeadline(id, deadline);
        return buildSuccessResponse(HttpStatus.OK, "Project deadline updated successfully");
    }

    @PatchMapping("/projects/{id}/updateEstimatedBudget")
    @Operation(summary = "Atualiza o orcamento estimado de um projeto")
    public ResponseEntity<ApiResponseDTO> updateProjectEstimatedBudget(@PathVariable Long id, @RequestBody BigDecimal estimatedBudget) {
        projectService.updateEstimatedBudget(id, estimatedBudget);
        return buildSuccessResponse(HttpStatus.OK, "Project estimated budget updated successfully");
    }
    @PatchMapping("/projects/{id}/updateStatus")
    @Operation(summary = "Atualiza o status de um projeto")
    public ResponseEntity<ApiResponseDTO> updateProjectStatus(@PathVariable Long id, @RequestBody ProjectStatus status) {
        projectService.updateStatus(id, status);
        return buildSuccessResponse(HttpStatus.OK, "Project status updated successfully");
    }

    @DeleteMapping("/projects/{id}")
    @Operation(summary = "Deleta um projeto")
    public ResponseEntity<ApiResponseDTO> deleteProject(@PathVariable Long id, @RequestBody String rawPassword) {
        projectService.deleteProject(id, rawPassword);
        return buildSuccessResponse(HttpStatus.OK, "Project deleted successfully");
    }
}
