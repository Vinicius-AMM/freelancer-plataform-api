package com.manager.freelancer_management_api.domain.proposal.util;

import com.manager.freelancer_management_api.domain.global.entities.Deadline;
import com.manager.freelancer_management_api.domain.proposal.dto.response.ProposalResponseDTO;
import com.manager.freelancer_management_api.domain.proposal.entity.Proposal;
import com.manager.freelancer_management_api.domain.user.entity.User;
import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import com.manager.freelancer_management_api.utils.common.PaginationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllProposalsHelperTest {
    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private GetAllProposalsHelper getAllProposalsHelper;

    private Pageable initialPageable;
    private Pageable effectivePageable;
    private User freelancer;
    private Proposal proposal1, proposal2;
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    @BeforeEach
    void setUp() {
        initialPageable = PageRequest.of(0, 10);
        effectivePageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD));

        freelancer = User.builder()
                .id(UUID.randomUUID())
                .fullName("Freelancer Test")
                .email("freelancer@test.com")
                .document("12345678901")
                .mainUserRole(UserRole.CLIENT)
                .currentUserRole(UserRole.FREELANCER)
                .build();

        proposal1 = Proposal.builder()
                .id(1L)
                .deadline(new Deadline(LocalDate.now(), LocalDate.now().plusDays(30)))
                .offeredValue(new BigDecimal("1000.00"))
                .freelancer(freelancer)
                .build();

        proposal2 = Proposal.builder()
                .id(2L)
                .deadline(new Deadline(LocalDate.now(), LocalDate.now().plusDays(30)))
                .offeredValue(new BigDecimal("2000.00"))
                .freelancer(freelancer)
                .build();
    }

    @Test
    @DisplayName("getAllProposals should return Page of ProposalResponseDTO successfully")
    void getAllProposals_shouldReturnPagedProposalResponseDTOs() {
        List<Proposal> proposals = List.of(proposal1, proposal2);
        Page<Proposal> proposalPage = new PageImpl<>(proposals, effectivePageable, proposals.size());

        when(paginationHelper.preparePageRequest(initialPageable, DEFAULT_SORT_FIELD)).thenReturn(effectivePageable);

        Function<Pageable, Page<Proposal>> fetcher = (pg) -> {
            if (pg.equals(effectivePageable)) {
                return proposalPage;
            }
            return Page.empty();
        };

        Page<ProposalResponseDTO> resultPage = getAllProposalsHelper.getAllProposals(initialPageable, fetcher);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());
        assertEquals(effectivePageable, resultPage.getPageable());

        List<Long> resultIds = resultPage.getContent().stream().map(ProposalResponseDTO::id).collect(Collectors.toList());
        assertTrue(resultIds.contains(proposal1.getId()));
        assertTrue(resultIds.contains(proposal2.getId()));

        ProposalResponseDTO resultDto1 = resultPage.getContent().stream().filter(dto -> dto.id().equals(proposal1.getId())).findFirst().orElse(null);
        assertNotNull(resultDto1);
        assertEquals(0, proposal1.getOfferedValue().compareTo(resultDto1.offeredValue()));
        assertEquals(proposal1.getFreelancer().getFullName(), resultDto1.freelancerProfile().fullName());

        verify(paginationHelper).preparePageRequest(initialPageable, DEFAULT_SORT_FIELD);
    }

    @Test
    @DisplayName("getAllProposals should return empty Page if fetcher returns empty")
    void getAllProposals_shouldReturnEmptyPage_whenFetcherReturnsEmpty() {
        Page<Proposal> emptyProposalPage = Page.empty(effectivePageable);

        when(paginationHelper.preparePageRequest(initialPageable, DEFAULT_SORT_FIELD)).thenReturn(effectivePageable);
        Function<Pageable, Page<Proposal>> fetcher = (pg) -> emptyProposalPage;

        Page<ProposalResponseDTO> resultPage = getAllProposalsHelper.getAllProposals(initialPageable, fetcher);

        assertNotNull(resultPage);
        assertTrue(resultPage.isEmpty());
        assertEquals(0, resultPage.getTotalElements());

        verify(paginationHelper).preparePageRequest(initialPageable, DEFAULT_SORT_FIELD);
    }
}