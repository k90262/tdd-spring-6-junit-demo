package com.example.demo.controller;

import com.example.demo.dto.TicketDto;
import com.example.demo.dto.TicketFilterDto;
import com.example.demo.exception.AgentNotFoundException;
import com.example.demo.exception.InvalidTicketStateException;
import com.example.demo.exception.MissingResolutionSummaryException;
import com.example.demo.exception.TicketNotFoundException;
import com.example.demo.model.Status;
import com.example.demo.service.TicketService;
import com.example.demo.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public void givenTicketNotInNewState_whenAssigningAgent_thenThrowException() throws Exception {
        Long ticketId = 1L;
        Long agentId = 1L;

        when(ticketService.assignAgentToTicket(ticketId, agentId)).thenThrow(new InvalidTicketStateException(Constants.ONLY_NEW_TICKET_CAN_BE_ASSIGNED_TO_AN_AGENT));

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", ticketId, agentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Constants.ONLY_NEW_TICKET_CAN_BE_ASSIGNED_TO_AN_AGENT));
    }

    @Test
    public void givenNonexistentAgent_whenAssigningToTicket_thenThrowException() throws Exception {
        Long ticketId = 1L;
        Long agentId = 99L;

        when(ticketService.assignAgentToTicket(ticketId, agentId))
                .thenThrow(new AgentNotFoundException(Constants.AGENT_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", ticketId, agentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Constants.AGENT_NOT_FOUND));
    }

    @Test
    public void givenNonexistentTicket_whenAssigningToAgent_thenThrowException() throws Exception {
        Long nonexistentTicketId = 999L;
        Long agentId = 1L;

        when(ticketService.assignAgentToTicket(nonexistentTicketId, agentId))
                .thenThrow(new TicketNotFoundException(Constants.TICKET_NOT_FOUND));

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", nonexistentTicketId, agentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(Constants.TICKET_NOT_FOUND));
    }

    @Test
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
    public void givenResolvedTicketWithoutSummary_whenClosing_thenThrowException() throws Exception {
        Long ticketId = 1L;

        when(ticketService.closeTicket(ticketId))
                .thenThrow(new MissingResolutionSummaryException(Constants.RESOLUTION_SUMMARY_REQUIRED));

        mockMvc.perform(put("/tickets/{id}/close", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Constants.RESOLUTION_SUMMARY_REQUIRED));
    }

    @Test
    public void givenClosedTicketWithoutSummary_whenResolving_thenThrowException() throws Exception {
        Long ticketId = 1L;

        when(ticketService.resolveTicket(ticketId))
                .thenThrow(new InvalidTicketStateException(Constants.ONLY_TICKET_IN_PROGRESS_CAN_BE_RESOLVED));

        mockMvc.perform(put("/tickets/{id}/resolve", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Constants.ONLY_TICKET_IN_PROGRESS_CAN_BE_RESOLVED));
    }

    @Test
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
    public void givenClosedTicket_whenUpdating_thenThrowException() throws Exception {
        Long ticketId = 1L;
        String ticketDescription = "Updated ticket description";
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.CLOSED, null, null, null, null);

        when(ticketService.updateTicket(eq(ticketId), any(TicketDto.class)))
                .thenThrow(new InvalidTicketStateException(Constants.CLOSED_TICKETS_CANNOT_BE_UPDATED));

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Constants.CLOSED_TICKETS_CANNOT_BE_UPDATED));
    }

    @Test
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
}
