package com.example.demo.controller;

import com.example.demo.dto.TicketDto;
import com.example.demo.exception.AgentNotFoundException;
import com.example.demo.exception.InvalidTicketStateException;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    public void givenTicketDetails_whenTicketIsCreted_thenTicketIsSaved() throws Exception {
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
        TicketDto ticketDto = new TicketDto(ticketId, ticketDescription, Status.IN_PROCESS, null, null, agentName, null);

        when(ticketService.assignAgentToTicket(ticketId, agentId)).thenReturn(ticketDto);

        mockMvc.perform(put("/tickets/{id}/assign/{agentId}", ticketId, agentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(Status.IN_PROCESS.name())))
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
}
