package com.example.demo.service;

import com.example.demo.dto.TicketDto;
import com.example.demo.exception.MissingDescriptionException;
import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import com.example.demo.repository.TicketRepository;
import com.example.demo.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @BeforeEach
    void setup() {
        ticketService = new TicketServiceImpl(ticketRepository);
    }

    @Test
    void givenTicketDetails_whenTicketIsCreated_thenCallsRepositorySave() {
        TicketDto ticketDto = new TicketDto(null, "description", null, null, null, null, null);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(new Ticket());

        ticketService.createTicket(ticketDto);

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void givenTicketDetails_whenTicketIsCreated_thenSetNewStatusAndCreationDate() {
        String description = "description";
        TicketDto ticketDto = new TicketDto(null, description, null, null, null, null, null);

        Ticket savedTicket = new Ticket(1L, description, Status.NEW, LocalDateTime.now());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketDto createdTicket = ticketService.createTicket(ticketDto);

        assertNotNull(createdTicket);
        assertEquals(Status.NEW, createdTicket.status());
        assertNotNull(createdTicket.createdDate());
    }

    @Test
    void givenTicketWithoutDetails_whenTicketIsCreated_thenThrowException() {
        TicketDto ticketDto = new TicketDto(null, null, null, null, null, null, null);

        assertThrows(MissingDescriptionException.class, () -> ticketService.createTicket(ticketDto));
    }
}
