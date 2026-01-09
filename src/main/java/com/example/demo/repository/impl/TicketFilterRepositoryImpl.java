package com.example.demo.repository.impl;

import com.example.demo.model.Agent;
import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import com.example.demo.repository.TicketFilterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

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

        if (startDate != null && endDate != null) {
            predicates.add(cb.between(ticketRoot.get("createdDate"), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(ticketRoot.get("createdDate"), startDate));
        } else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(ticketRoot.get("createdDate"), endDate));
        }

        Join<Ticket, Agent> agentJoin = ticketRoot.join("assignedAgent", JoinType.LEFT);
        if (assignedAgent != null && !assignedAgent.trim().isEmpty()) {
            predicates.add(cb.equal(agentJoin.get("name"), assignedAgent));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }
}
