package com.manager.freelancer_management_api.domain.proposal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.project.entity.Project;
import com.manager.freelancer_management_api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "proposals")
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deadline", nullable = false)
    @Embedded
    private Deadline deadline;

    @Column(name = "offered_value", nullable = false)
    private BigDecimal offeredValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User freelancer;
}