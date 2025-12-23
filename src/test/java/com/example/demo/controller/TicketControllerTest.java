package com.example.demo.controller;

import com.example.demo.dto.TicketDto;
import com.example.demo.dto.TicketFilterDto;
import com.example.demo.exception.*;
import com.example.demo.model.Status;
import com.example.demo.service.TicketService;
import com.example.demo.util.ErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    @Test
    @DisplayName("Given ticket details are provided, when a new ticket is created, then the ticket is successfully saved")
    public void givenTicketDetails_whenTicketIsCreated_thenTicketIsSaved() throws Exception {
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto = new TicketDto(null, ticketDescription, Status.NEW, null, null, null, null);

        when(ticketService.createTicket(any(TicketDto.class))).thenReturn(ticketDto);

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is(ticketDescription)))
                .andExpect(jsonPath("$.status", is(Status.NEW.name())));
    }

    @Test
    @DisplayName("Given a new ticket, when an agent is assigned, then the ticket status is updated to 'IN_PROGRESS'")
    public void givenNewTicket_whenAssigningAgent_thenStatusIsInProcess() throws Exception {
        Long ticketId = 1L;
        Long agentId = 1L;
        String agentName = "Agent001";
        String ticketDescription = "Description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.IN_PROGRESS, null, null, agentName, null);

        when(ticketService.assignAgentToTicket(ticketId, agentId)).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", ticketId, agentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.IN_PROGRESS.name())))
                .andExpect(jsonPath("$.assignedAgent", is(agentName)));
    }

    @Test
    @DisplayName("Given a ticket not in 'NEW' state, when an agent is assigned, then an InvalidTicketStateException is thrown")
    public void givenTicketNotInNewState_whenAssigningAgent_thenThrowException() throws Exception {
        Long ticketId = 1L;
        Long agentId = 1L;

        when(ticketService.assignAgentToTicket(ticketId, agentId)).thenThrow(new InvalidTicketStateException(ErrorMessages.ONLY_NEW_TICKET_CAN_BE_ASSIGNED_TO_AN_AGENT));

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", ticketId, agentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ONLY_NEW_TICKET_CAN_BE_ASSIGNED_TO_AN_AGENT));
    }

    @Test
    @DisplayName("Given a nonexistent agent, when assigning to a ticket, then an AgentNotFoundException is thrown")
    public void givenNonexistentAgent_whenAssigningToTicket_thenThrowException() throws Exception {
        Long ticketId = 1L;
        Long agentId = 99L;

        when(ticketService.assignAgentToTicket(ticketId, agentId))
                .thenThrow(new AgentNotFoundException(ErrorMessages.AGENT_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", ticketId, agentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.AGENT_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when an agent is assigned, then a TicketNotFoundException is thrown")
    public void givenNonexistentTicket_whenAssigningToAgent_thenThrowException() throws Exception {
        Long nonexistentTicketId = 999L;
        Long agentId = 1L;

        when(ticketService.assignAgentToTicket(nonexistentTicketId, agentId))
                .thenThrow(new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", nonexistentTicketId, agentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a ticket in 'IN_PROGRESS' state, when resolving the ticket, then the status is updated to 'RESOLVED'")
    public void givenTicketInProgress_whenResolving_thenStatusIsInResolved() throws Exception {
        Long ticketId = 1L;
        String agentName = "Agent001";
        String ticketDescription = "Description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.RESOLVED, null, null, agentName, null);

        when(ticketService.resolveTicket(ticketId)).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}/resolve", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.RESOLVED.name())));
    }

    @Test
    @DisplayName("Given a resolved ticket with a summary, when closing the ticket, then the status is updated to 'CLOSED'")
    public void givenResolvedTicketWithSummary_whenClosing_thenStatusIsClosed() throws Exception {
        Long ticketId = 1L;
        String agentName = "Agent001";
        String ticketDescription = "Description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.CLOSED, null, null, agentName, "Issue resolved");

        when(ticketService.closeTicket(ticketId)).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}/close", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.CLOSED.name())));
    }

    @Test
    @DisplayName("Given a resolved ticket without a summary, when closing the ticket, then a MissingResolutionSummaryException is thrown")
    public void givenResolvedTicketWithoutSummary_whenClosing_thenThrowException() throws Exception {
        Long ticketId = 1L;

        when(ticketService.closeTicket(ticketId))
                .thenThrow(new MissingResolutionSummaryException(ErrorMessages.RESOLUTION_SUMMARY_REQUIRED));

        mockMvc.perform(put("/tickets/{id}/close", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.RESOLUTION_SUMMARY_REQUIRED));
    }

    @Test
    @DisplayName("Given a closed ticket, when resolving the ticket, then an InvalidTicketStateException is thrown")
    public void givenClosedTicketWithoutSummary_whenResolving_thenThrowException() throws Exception {
        Long ticketId = 1L;

        when(ticketService.resolveTicket(ticketId))
                .thenThrow(new InvalidTicketStateException(ErrorMessages.ONLY_TICKET_IN_PROGRESS_CAN_BE_RESOLVED));

        mockMvc.perform(put("/tickets/{id}/resolve", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ONLY_TICKET_IN_PROGRESS_CAN_BE_RESOLVED));
    }

    @Test
    @DisplayName("Given a ticket details, when the ticket is updated, then the details are successfully updated")
    public void givenTicketDetails_whenTicketIsUpdated_thenDetailsAreUpdated() throws Exception {
        Long ticketId = 1L;
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.NEW, null, null, null, null);

        when(ticketService.updateTicket(eq(ticketId), any(TicketDto.class)))
                .thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(ticketDescription)));
    }

    @Test
    @DisplayName("Given a closed ticket, when updating the ticket, then an InvalidTicketStateException is thrown")
    public void givenClosedTicket_whenUpdating_thenThrowException() throws Exception {
        Long ticketId = 1L;
        String ticketDescription = "Updated ticket description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.CLOSED, null, null, null, null);

        when(ticketService.updateTicket(eq(ticketId), any(TicketDto.class)))
                .thenThrow(new InvalidTicketStateException(ErrorMessages.CLOSED_TICKETS_CANNOT_BE_UPDATED));

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.CLOSED_TICKETS_CANNOT_BE_UPDATED));
    }

    @Test
    @DisplayName("Given a valid ticket ID, when getting the ticket, then the ticket details are returned")
    public void givenValidTicketId_whenGettingTicket_thenReturnsTicketDetails() throws Exception {
        Long ticketId = 1L;
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.NEW, null, null, null, null);

        when(ticketService.getTicketById(ticketId)).thenReturn(ticketDto);

        mockMvc.perform(get("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticketId.intValue())));
    }

    @Test
    @DisplayName("Given filter criteria, when getting tickets, then the returned tickets match the filter criteria")
    public void givenFilterCriteria_whenGettingTickets_thenReturnsFilteredTickets() throws Exception {
        String agentName = "Agent001";
        String ticketDescription = "Sample ticket description";
        TicketDto ticketDto1 = new TicketDto(1L, ticketDescription, Status.NEW, LocalDateTime.now(), null, agentName, null);
        TicketDto ticketDto2 = new TicketDto(2L, ticketDescription, Status.NEW, LocalDateTime.now().minusDays(2), null, agentName, null);

        List<TicketDto> filteredTickets = List.of(ticketDto1, ticketDto2);

        when(ticketService.getTickets(any(TicketFilterDto.class))).thenReturn(filteredTickets);

        mockMvc.perform(get("/tickets")
                        .param("status", "NEW,IN_PROGRESS")
                        .param("startDate", LocalDateTime.now().minusDays(3).toString())
                        .param("endDate", LocalDateTime.now().toString())
                        .param("assignedAgent", agentName)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(filteredTickets.size())))
                .andExpect(jsonPath("$[0].id", is(ticketDto1.id().intValue())))
                .andExpect(jsonPath("$[1].id", is(ticketDto2.id().intValue())));
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when resolving the ticket, then a TicketNotFoundException is thrown")
    public void givenNonexistentTicket_whenResolving_thenThrowException() throws Exception {
        Long nonexistentTicketId = 999L;

        when(ticketService.resolveTicket(nonexistentTicketId))
                .thenThrow(new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/resolve", nonexistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when closing the ticket, then a TicketNotFoundException is thrown")
    public void givenNonexistentTicket_whenClosisng_thenThrowException() throws Exception {
        Long nonexistentTicketId = 999L;

        when(ticketService.closeTicket(nonexistentTicketId))
                .thenThrow(new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/close", nonexistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a ticket not in resolved state, when closing the ticket, then an InvalidTicketStateException is thrown")
    public void givenTicketNotResolvedState_whenClosing_thenThrowException() throws Exception {
        Long ticketId = 1L;

        when(ticketService.closeTicket(ticketId))
                .thenThrow(new InvalidTicketStateException(ErrorMessages.ONLY_RESOLVED_TICKET_CAN_BE_CLOSED));

        mockMvc.perform(put("/tickets/{id}/close", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ONLY_RESOLVED_TICKET_CAN_BE_CLOSED));
    }

    @Test
    @DisplayName("Given a nonexistent ticket, when updating the ticket, then a TicketNotFoundException is thrown")
    public void givenNonexistentTicket_whenUpdating_thenThrowException() throws Exception {
        Long nonexistentTicketId = 999L;
        String ticketDescription = "Updated ticket description";
        TicketDto ticketDto = new TicketDto(nonexistentTicketId, ticketDescription, Status.NEW, null, null, null, null);

        when(ticketService.updateTicket(nonexistentTicketId, ticketDto))
                .thenThrow(new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}", nonexistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given a nonexistent ticket ID, when getting the ticket, then a TicketNotFoundException is thrown")
    public void givenNonexistentTicket_whenGettingTicket_thenThrowException() throws Exception {
        Long nonexistentTicketId = 999L;

        when(ticketService.getTicketById(nonexistentTicketId))
                .thenThrow(new TicketNotFoundException(ErrorMessages.TICKET_NOT_FOUND));

        mockMvc.perform(get("/tickets/{id}", nonexistentTicketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(ErrorMessages.TICKET_NOT_FOUND));
    }

    @Test
    @DisplayName("Given an invalid date range, when getting tickets, then an InvalidDateRangeException is thrown")
    public void givenInvalidDataRange_whenGettingTicket_thenThrowException() throws Exception {
        when(ticketService.getTickets(any(TicketFilterDto.class)))
                .thenThrow(new InvalidDateRangeException(ErrorMessages.INVALID_DATE_RANGE));

        mockMvc.perform(get("/tickets")
                        .param("startDate", LocalDateTime.now().toString())
                        .param("endDate", LocalDateTime.now().minusDays(3).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.INVALID_DATE_RANGE));
    }

    @Test
    @DisplayName("Given a ticket without a description, when a new ticket is created, then a MissingDescriptionException is thrown")
    public void givenTicketWithoutDescription_whenTicketIsCreated_thenThrowException() throws Exception {
        TicketDto ticketDto = new TicketDto(null, null, Status.NEW, null, null, null, null);

        when(ticketService.createTicket(any(TicketDto.class)))
                .thenThrow(new MissingDescriptionException(ErrorMessages.DESCRIPTION_REQUIRED));

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.DESCRIPTION_REQUIRED));
    }
}
