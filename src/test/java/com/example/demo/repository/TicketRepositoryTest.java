package com.example.demo.repository;

import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
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
    void givenStatus_whenGettingTickets_thenTicketsWithMatchingStatusAreReturned() {
        List<Ticket> inProgressTickets = ticketRepository.findWithFilters(
                List.of(Status.IN_PROGRESS), null, null, null
        );

        assertEquals(1, inProgressTickets.size());
    }

    @Test
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
}
