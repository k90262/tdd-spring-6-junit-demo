package com.example.demo.service.impl;

import com.example.demo.dto.TicketDto;
import com.example.demo.dto.TicketFilterDto;
import com.example.demo.model.Ticket;
import com.example.demo.repository.TicketRepository;
import com.example.demo.service.TicketService;

import java.util.List;

public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
        Ticket newTicket = new Ticket();

        Ticket savedTicket = ticketRepository.save(newTicket);

        return null;
    }

    @Override
    public TicketDto assignAgentToTicket(Long ticketId, Long agentId) {
        return null;
    }

    @Override
    public TicketDto resolveTicket(Long ticketId) {
        return null;
    }

    @Override
    public TicketDto closeTicket(Long ticketId) {
        return null;
    }

    @Override
    public TicketDto updateTicket(Long ticketId, TicketDto ticketDto) {
        return null;
    }

    @Override
    public TicketDto getTicketById(Long ticketId) {
        return null;
    }

    @Override
    public List<TicketDto> getTickets(TicketFilterDto ticketFilterDto) {
        return List.of();
    }
}
