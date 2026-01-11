package com.example.demo.repository;

import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Sql("/filterTestData.sql")
public class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void firstTest() {
        assertNotNull(ticketRepository);
    }

    @Test
    @DisplayName("When filtering by status, tickets with matching status are returned")
    void givenStatus_whenGettingTickets_thenTicketsWithMatchingStatusAreReturned() {
        List<Ticket> inProgressTickets = ticketRepository.findWithFilters(
                List.of(Status.IN_PROGRESS),
                null,
                null,
                null
        );

        assertEquals(1, inProgressTickets.size());
    }

    @Test
    @DisplayName("When filtering by a date range, tickets within that range are returned")
    void givenDateRange_whenGettingTickets_thenTicketsWithinRangeAreReturned() {
        LocalDateTime now = LocalDateTime.now();

        List<Ticket> inProgressTickets = ticketRepository.findWithFilters(
                null,
                now.minusDays(3),
                now,
                null
        );

        assertEquals(3, inProgressTickets.size());
    }

    @Test
    @DisplayName("When filtering with a start date, tickets created after that date are returned")
    void givenStartDate_whenGettingTickets_thenTicketsAfterStartDateAreReturned() {
        LocalDateTime now = LocalDateTime.now();

        List<Ticket> ticketsCreatedAfterLastThreeDays = ticketRepository.findWithFilters(
                null,
                now.minusDays(3),
                null,
                null
        );

        assertEquals(3, ticketsCreatedAfterLastThreeDays.size());
    }

    @Test
    @DisplayName("When filtering with an end date, tickets created before that date are returned")
    void givenEndDate_whenGettingTickets_thenTicketsBeforeEndDateAreReturned() {
        LocalDateTime now = LocalDateTime.now();

        List<Ticket> ticketsCreatedBeforeLastThreeDays = ticketRepository.findWithFilters(
                null,
                null,
                now.minusDays(3),
                null
        );

        assertEquals(2, ticketsCreatedBeforeLastThreeDays.size());
    }

    @Test
    @DisplayName("When filtering by agent, tickets assigned to that agent are returned")
    void givenAgent_whenGettingTickets_thenTicketsMatchingAgentAreReturned() {
        String agentName = "Agent002";

        List<Ticket> ticketsWithAgentAssigned = ticketRepository.findWithFilters(
                null,
                null,
                null,
                agentName
        );

        assertEquals(2, ticketsWithAgentAssigned.size());
        for (Ticket ticket : ticketsWithAgentAssigned) {
            assertNotNull(ticket.getAssignedAgent());
            assertEquals(agentName, ticket.getAssignedAgent().getName());
        }
    }

    @Test
    @DisplayName("When no filters are applied, all tickets are returned")
    void givenNoFilter_whenGettingTickets_thenAllTicketsAreReturned() {
        List<Ticket> tickets = ticketRepository.findWithFilters(
                null,
                null,
                null,
                null
        );

        assertEquals(5, tickets.size());
    }

    @Test
    @DisplayName("When multiple filters are applied, only the tickets matching those filters are returned")
    void givenMultipleFilters_whenGettingTickets_thenMatchingTicketsAreReturned() {
        List<Ticket> tickets = ticketRepository.findWithFilters(
                List.of(Status.NEW, Status.RESOLVED),
                null,
                LocalDateTime.of(2023, 6, 30, 0, 0),
                null
        );

        assertEquals(2, tickets.size());
    }
}
