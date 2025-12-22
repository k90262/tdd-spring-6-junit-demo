package com.example.demo.dto;

import com.example.demo.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public record TicketFilterDto(List<Status> status,
                              LocalDateTime startDate,
                              LocalDateTime endDate,
                              String assignedAgent) {
}