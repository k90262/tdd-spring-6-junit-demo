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
}
