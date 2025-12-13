package com.example.demo.dto;

import com.example.demo.model.Status;

import java.time.LocalDateTime;

public record TicketDto(Long id,
                        String description,
                        Status status,
                        LocalDateTime createdDate,
                        LocalDateTime closedDate,
                        String assignedAgent,
                        String resolutionSummary) {
}
