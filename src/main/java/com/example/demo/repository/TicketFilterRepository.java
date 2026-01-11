package com.example.demo.repository;

import com.example.demo.model.Status;
import com.example.demo.model.Ticket;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketFilterRepository {
    List<Ticket> findWithFilters(List<Status> statuses,
                                 LocalDateTime startDate,
                                 LocalDateTime endDate,
                                 String assignedAgent);
}
