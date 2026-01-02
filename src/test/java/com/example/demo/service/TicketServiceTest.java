package com.example.demo.service;

import com.example.demo.dto.TicketDto;
import com.example.demo.dto.TicketFilterDto;
import com.example.demo.exception.*;
import com.example.demo.model.Agent;
import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AgentRepository agentRepository;

    @BeforeEach
    void setup() {
        ticketService = new TicketServiceImpl(ticketRepository, agentRepository);
    }

    @Test
    @DisplayName("Given ticket details are provided, when a new ticket is created, then the repository's save method is called")
    void givenTicketDetails_whenTicketIsCreated_thenCallsRepositorySave() {
        TicketDto ticketDto = new TicketDto(null, "description", null, null, null, null, null);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(new Ticket());

        ticketService.createTicket(ticketDto);

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Given ticket details are provided, when a new ticket is created, then the NEW status and the creation date is saved")
    void givenTicketDetails_whenTicketIsCreated_thenSetNewStatusAndCreationDate() {
        String description = "description";
        TicketDto ticketDto = new TicketDto(null, description, null, null, null, null, null);

        Ticket savedTicket = new Ticket(1L, description, Status.NEW, LocalDateTime.now());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto createdTicket = ticketService.createTicket(ticketDto);

        assertNotNull(createdTicket);
        assertEquals(Status.NEW, createdTicket.status());
        assertNotNull(createdTicket.createdDate());
    }

    @Test
    @DisplayName("Given a ticket without a description, when a new ticket is created, then a MissingDescriptionException is thrown")
    void givenTicketWithoutDetails_whenTicketIsCreated_thenThrowException() {
        TicketDto ticketDto = new TicketDto(null, null, null, null, null, null, null);

        assertThrows(MissingDescriptionException.class, () -> ticketService.createTicket(ticketDto));
    }

    @Test
    @DisplayName("Given a new ticket, when an agent is assigned, then the ticket status is updated to 'IN_PROGRESS'")
    void givenNewTicket_whenAssigningAgent_thenStatusIsInProgress() {
        Long ticketId = 1L;
        Long agentId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());
        Agent agent = new Agent(agentId, "Agent001");
        Ticket savedTicket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());
        savedTicket.setAssignedAgent(agent);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        TicketDto updatedTicket = ticketService.assignAgentToTicket(ticketId, agentId);

        assertEquals(ticketId, updatedTicket.id());
        assertEquals(agentId, agent.getId());
        assertEquals(Status.IN_PROGRESS, updatedTicket.status());
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when assigning to a ticket, then a TicketNotFoundException is thrown")
    void givenNonexistentTicket_whenAssigningAgent_thenThrowException() {
        Long nonexistentTicketId = 999L;
        Long agentId = 1L;

        when(ticketRepository.findById(nonexistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.assignAgentToTicket(nonexistentTicketId, agentId));
    }

    @Test
    @DisplayName("Given a nonexistent agent, when assigning to a ticket, then an AgentNotFoundException is thrown")
    void givenNonexistentAgent_whenAssigningToTicket_thenThrowException() {
        Long ticketId = 999L;
        Long nonexistentAgentId = 1L;
        String ticketDescription = "description";
        Ticket ticket = new Ticket(ticketId, ticketDescription, Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(agentRepository.findById(nonexistentAgentId)).thenReturn(Optional.empty());

        assertThrows(AgentNotFoundException.class,
                () -> ticketService.assignAgentToTicket(ticketId, nonexistentAgentId));
    }

    @Test
    @DisplayName("Given a ticket not in 'NEW' state, when an agent is assigned, then an InvalidTicketStateException is thrown")
    void givenTicketNotInNewState_whenAssigningAgent_thenThrowException() {
        Long ticketId = 1L;
        Long agentId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.assignAgentToTicket(ticketId, agentId));
    }

    @Test
    @DisplayName("Given a ticket in 'IN_PROGRESS' state, when resolving the ticket, then the status is updated to 'RESOLVED'")
    void givenTicketInProgress_whenResolving_thenStatusIsResolved() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.IN_PROGRESS, LocalDateTime.now());
        Ticket savedTicket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto updatedTicket = ticketService.resolveTicket(ticketId);

        assertEquals(Status.RESOLVED, updatedTicket.status());
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when resolving the ticket, then a TicketNotFoundException is thrown")
    void givenNonexistentTicket_whenResolving_thenThrowException() {
        Long nonexistentTicketId = 999L;

        when(ticketRepository.findById(nonexistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.resolveTicket(nonexistentTicketId));
    }

    @Test
    @DisplayName("Given a ticket not in 'IN_PROGRESS' state, when resolving the ticket, then an InvalidTicketStateException is thrown")
    void givenTicketNotInProgressState_whenResolving_thenThrowException() {
        Long ticketId = 1L;
        String description = "description";
        Ticket ticket = new Ticket(ticketId, description, Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.resolveTicket(ticketId));
    }

    @Test
    @DisplayName("Given a resolved ticket with a summary, when closing the ticket, then the status is updated to 'CLOSED'")
    void givenResolvedTicketWithSummary_whenClosing_thenStatusIsClosed() {
        Long ticketId = 1L;
        String description = "description";
        String resolutionSummary = "resolution summary";
        Ticket ticket = new Ticket(ticketId, description, Status.RESOLVED, LocalDateTime.now());
        ticket.setResolutionSummary(resolutionSummary);
        Ticket savedTicket = new Ticket(ticketId, description, Status.CLOSED, LocalDateTime.now());
        savedTicket.setResolutionSummary(resolutionSummary);
        savedTicket.setClosedDate(LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto updatedTicket = ticketService.closeTicket(ticketId);

        assertEquals(Status.CLOSED, updatedTicket.status());
        assertNotNull(updatedTicket.closedDate());
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when closing the ticket, then a TicketNotFoundException is thrown")
    void givenNonexistentTicket_whenClosing_thenThrowException() {
        Long nonexistentTicketId = 999L;

        when(ticketRepository.findById(nonexistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.closeTicket(nonexistentTicketId));
    }

    @Test
    @DisplayName("Given a resolved ticket without a summary, when closing the ticket, then a MissingResolutionSummaryException is thrown")
    void givenResolvedTicketWithoutSummary_whenClosing_thenThrowException() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "description", Status.RESOLVED, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(MissingResolutionSummaryException.class,
                () -> ticketService.closeTicket(ticketId));
    }

    @Test
    @DisplayName("Given a ticket not in resolved state, when closing the ticket, then an InvalidTicketStateException is thrown")
    void givenTicketNotInResolvedState_whenClosing_thenThrowException() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "description", Status.NEW, LocalDateTime.now());
        ticket.setResolutionSummary("Summary");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.closeTicket(ticketId));
    }

    @Test
    @DisplayName("Given a ticket description and resolution summary, when updating the ticket, then the description and resolution summary are updated")
    void givenTicketDescriptionAndResolutionSummary_whenUpdating_thenDescriptionAndResolutionSummaryAreUpdated() {
        Long ticketId = 1L;
        LocalDateTime now = LocalDateTime.now();
        TicketDto ticketDto = new TicketDto(ticketId, "Updated description", null, null, null, null, "Updated summary");
        Ticket originalTicket = new Ticket(ticketId, "Description", Status.RESOLVED, now);
        originalTicket.setResolutionSummary("Summary");
        Ticket updatedTicketFromRepo = new Ticket(ticketId, ticketDto.description(), Status.RESOLVED, now);
        updatedTicketFromRepo.setResolutionSummary(ticketDto.resolutionSummary());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(originalTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(updatedTicketFromRepo);

        TicketDto updatedTicket = ticketService.updateTicket(ticketId, ticketDto);

        assertEquals(ticketDto.description(), updatedTicket.description());
        assertEquals(ticketDto.resolutionSummary(), updatedTicket.resolutionSummary());
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when updating the ticket, then a TicketNotFoundException is thrown")
    void givenNonexistentTicket_whenUpdating_thenThrowException() {
        Long nonexistentTicketId = 999L;
        TicketDto ticketDto = new TicketDto(nonexistentTicketId, "Updated description", null, null, null, null, "Updated summary");

        when(ticketRepository.findById(nonexistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.updateTicket(nonexistentTicketId, ticketDto));
    }

    @Test
    @DisplayName("Given a closed ticket, when updating the ticket, then an InvalidTicketStateException is thrown")
    void givenClosedTicket_whenUpdating_thenThrowException() {
        Long ticketId = 1L;
        String ticketDescription = "Ticket Description";
        String ticketResolutionSummary = "Resolution Summary";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, null, null, null, null, ticketResolutionSummary);
        Ticket ticket = new Ticket(ticketId, ticketDescription, Status.CLOSED, LocalDateTime.now());
        ticket.setResolutionSummary(ticketResolutionSummary);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidTicketStateException.class,
                () -> ticketService.updateTicket(ticketId, ticketDto));
    }

    @Test
    @DisplayName("Given a valid ticket ID, when getting the ticket, then the ticket details are returned")
    void givenValidTicketId_whenGettingTicket_thenReturnTicketDetails() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "description", Status.NEW, LocalDateTime.now());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        TicketDto ticketDto = ticketService.getTicketById(ticketId);

        assertEquals(ticketId, ticketDto.id());
    }

    @Test
    @DisplayName("Given a nonexistent ticket ID, when getting the ticket, then a TicketNotFoundException is thrown")
    void givenNonexistentTicket_whenGettingTicket_thenThrowException() {
        Long nonexistentTicketId = 999L;

        when(ticketRepository.findById(nonexistentTicketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class,
                () -> ticketService.getTicketById(nonexistentTicketId)
        );
    }

    @Test
    @DisplayName("Given filter criteria, when getting tickets, then the returned tickets match the filter criteria")
    void givenFilterCriteria_whenGettingTickets_thenReturnFilteredTickets() {
        TicketFilterDto filterDto = new TicketFilterDto(List.of(Status.NEW), null, null, null);
        List<Ticket> filteredTickets = List.of(
                new Ticket(1L, "Ticket 1", Status.NEW, LocalDateTime.now()),
                new Ticket(2L, "Ticket 2", Status.NEW, LocalDateTime.now())
        );

        when(ticketRepository.findWithFilters(anyList(), any(), any(), any())).thenReturn(filteredTickets);

        List<TicketDto> ticketDtos = ticketService.getTickets(filterDto);

        assertEquals(2, ticketDtos.size());
    }

    @Test
    @DisplayName("Given an invalid date range, when getting tickets, then an InvalidDateRangeException is thrown")
    void givenInvalidDateRange_whenGettingTickets_thenThrowException() {
        TicketFilterDto filterDto = new TicketFilterDto(
                null,
                LocalDateTime.of(2023, 6, 25, 0, 0),
                LocalDateTime.of(1999, 6, 25, 0, 0),
                null
        );

        assertThrows(InvalidDateRangeException.class, () -> ticketService.getTickets(filterDto));
    }
}
