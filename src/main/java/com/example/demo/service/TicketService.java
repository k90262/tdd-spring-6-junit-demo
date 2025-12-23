package com.example.demo.service;

import com.example.demo.dto.TicketDto;
import com.example.demo.dto.TicketFilterDto;
import com.example.demo.exception.*;

import java.util.List;

public interface TicketService {
    /**
     * Creates a new ticket.
     *
     * @param ticketDto The data transfer object containing ticket information.
     * @return The created TicketDto.
     * @throws MissingDescriptionException if the description is missing in the provided TicketDto.
     */
    TicketDto createTicket(TicketDto ticketDto);

    /**
     * Assigns an agent to a ticket.
     *
     * @param ticketId The ID of the ticket to which an agent is to be assigned.
     * @param agentId The ID of the agent to be assigned.
     * @return The updated TicketDto with the agent assigned.
     * @throws AgentNotFoundException if the agent with the provided ID is not found.
     * @throws TicketNotFoundException if the ticket with the provided ID is not found.
     * @throws InvalidTicketStateException if the ticket is not in the 'NEW' state.
     */
    TicketDto assignAgentToTicket(Long ticketId, Long agentId);

    /**
     * Resolves a ticket.
     *
     * @param ticketId The ID of the ticket to be resolved.
     * @return The updated TicketDto marked as resolved.
     * @throws TicketNotFoundException if the ticket with the provided ID is not found.
     * @throws InvalidTicketStateException if the ticket is not in the 'IN_PROGRESS' state.
     */
    TicketDto resolveTicket(Long ticketId);

    /**
     * Closes a ticket.
     *
     * @param ticketId The ID of the ticket to be closed.
     * @return The updated TicketDto marked as closed.
     * @throws TicketNotFoundException if the ticket with the provided ID is not found.
     * @throws MissingResolutionSummaryException if the ticket doesn't have a resolution summary when is closed.
     * @throws InvalidTicketStateException if the ticket is not in the 'RESOLVED' state.
     */
    TicketDto closeTicket(Long ticketId);

    /**
     * Updates an existing ticket (only the description and the resolution summary).
     *
     * @param ticketId  The ID of the ticket to be updated.
     * @param ticketDto The data transfer object containing the updated ticket information.
     * @return The updated TicketDto.
     * @throws TicketNotFoundException if the ticket with the provided ID is not found.
     * @throws InvalidTicketStateException if the ticket is in the 'CLOSED' state and cannot be updated.
     */
    TicketDto updateTicket(Long ticketId, TicketDto ticketDto);

    /**
     * Retrieves a ticket by its ID.
     *
     * @param ticketId The ID of the ticket to retrieve.
     * @return The TicketDto corresponding to the provided ID.
     * @throws TicketNotFoundException if the ticket with the provided ID is not found.
     */
    TicketDto getTicketById(Long ticketId);

    /**
     * Retrieves a list of tickets based on the provided filter criteria.
     *
     * @param ticketFilterDto The data transfer object containing filter criteria.
     * @return A list of TicketDto objects matching the filter criteria.
     * @throws InvalidDateRangeException if the end date in the filter criteria is earlier than the start date.
     */
    List<TicketDto> getTickets(TicketFilterDto ticketFilterDto);
}
