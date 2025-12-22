package com.example.demo.service;

import com.example.demo.dto.TicketDto;
import com.example.demo.dto.TicketFilterDto;
import java.util.List;

public interface TicketService {
    TicketDto createTicket(TicketDto ticketDto);

    TicketDto assignAgentToTicket(Long ticketId, Long agentId);

    TicketDto resolveTicket(Long ticketId);

    TicketDto closeTicket(Long ticketId);

    TicketDto updateTicket(Long ticketId, TicketDto ticketDto);

    TicketDto getTicketById(Long ticketId);

    List<TicketDto> getTickets(TicketFilterDto ticketFilterDto);
}
