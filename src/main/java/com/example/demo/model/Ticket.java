package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Ticket {

    private Long id;

    private String description;

    private Status status;

    private LocalDateTime createdDate;

    private LocalDateTime closedDate;

    private String resolutionSummary;

    public Ticket() {}

    public Ticket(Long id, String description, Status status, LocalDateTime createdDate) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDateTime closedDate) {
        this.closedDate = closedDate;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id)
                && Objects.equals(description, ticket.description)
                && status == ticket.status
                && Objects.equals(createdDate, ticket.createdDate)
                && Objects.equals(closedDate, ticket.closedDate)
                && Objects.equals(resolutionSummary, ticket.resolutionSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, status, createdDate, closedDate, resolutionSummary);
    }
}
