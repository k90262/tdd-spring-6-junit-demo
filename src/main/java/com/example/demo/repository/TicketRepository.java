package com.example.demo.repository;

import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findWithFilters(List<Status> statuses,
                           LocalDateTime startDate,
                           LocalDateTime endDate,
                           String assignedAgent);
}
