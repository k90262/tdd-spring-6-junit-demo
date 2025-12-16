package com.example.demo.service;

import com.example.demo.dto.TicketDto;

public interface TicketService {
    TicketDto createTicket(TicketDto ticketDto);

    TicketDto assignAgentToTicket(Long ticketId, Long agentId);
}
