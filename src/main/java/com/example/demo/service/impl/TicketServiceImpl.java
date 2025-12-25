package com.example.demo.service.impl;

import com.example.demo.dto.TicketDto;
import com.example.demo.dto.TicketFilterDto;
import com.example.demo.exception.MissingDescriptionException;
import com.example.demo.model.Ticket;
import com.example.demo.repository.TicketRepository;
import com.example.demo.service.TicketService;
import com.example.demo.util.ErrorMessages;

import java.util.List;

public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
        if (ticketDto.description() == null || ticketDto.description().isEmpty()) {
            throw new MissingDescriptionException(ErrorMessages.DESCRIPTION_REQUIRED);
        }

        Ticket newTicket = new Ticket();
        newTicket.setDescription(ticketDto.description());
        newTicket.setStatus(ticketDto.status());
        newTicket.setCreatedDate(ticketDto.createdDate());

        Ticket savedTicket = ticketRepository.save(newTicket);

        return new TicketDto(
                savedTicket.getId(),
                savedTicket.getDescription(),
                savedTicket.getStatus(),
                savedTicket.getCreatedDate(),
                null,
                null,
                null);
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
