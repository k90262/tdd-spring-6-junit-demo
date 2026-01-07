package com.example.demo.repository;

import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void firstTest() {
        assertNotNull(ticketRepository);
    }

    @Test
    @Sql("/filterTestData.sql")
    void givenStatus_whenGettingTickets_thenTicketsWithMatchingStatusAreReturned() {
        List<Ticket> inProgressTickets = ticketRepository.findWithFilters(
                List.of(Status.IN_PROGRESS), null, null, null
        );

        assertEquals(1, inProgressTickets.size());
    }
}
