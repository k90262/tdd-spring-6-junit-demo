package com.example.demo.service;

import com.example.demo.dto.TicketDto;

public interface TicketService {
    TicketDto createTicket(TicketDto ticketDto);

    TicketDto assignAgentToTicket(Long ticketId, Long agentId);

    TicketDto resolveTicket(Long ticketId);

    TicketDto closeTicket(Long ticketId);

    TicketDto updateTicket(Long ticketId, TicketDto ticketDto);
}
