INSERT INTO agent (id, name) VALUES (1, 'Agent001');

INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (100, 'Ticket 100', 'NEW', '2023-01-01T09:00:00', NULL);
INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (101, 'Ticket 101', 'IN_PROGRESS', '2023-02-01T09:00:00', 1);
INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (102, 'Ticket 102', 'RESOLVED', '2023-03-01T09:00:00', 1);
INSERT INTO ticket (id, description, status, created_date, assigned_agent_id, resolution_summary)
VALUES (103, 'Ticket 103', 'RESOLVED', '2023-04-01T09:00:00', 1, 'Resolution Summary');
INSERT INTO ticket (id, description, status, created_date, closed_date, assigned_agent_id)
VALUES (104, 'Ticket 104', 'CLOSED', '2023-05-01T09:00:00', '2023-05-25T09:00:00', 1);
