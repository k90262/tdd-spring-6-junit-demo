package com.example.demo.repository.impl;

import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import com.example.demo.repository.TicketFilterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketFilterRepositoryImpl implements TicketFilterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Ticket> findWithFilters(List<Status> statuses,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        String assignedAgent) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Ticket> query = cb.createQuery(Ticket.class);
        Root<Ticket> ticketRoot = query.from(Ticket.class);

        List<Predicate> predicates = new ArrayList<>();

        if (statuses != null && !statuses.isEmpty()) {
            predicates.add(ticketRoot.get("status").in(statuses));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }
}
