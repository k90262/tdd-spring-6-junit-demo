INSERT INTO agent (id, name) VALUES (1, 'Agent001');
INSERT INTO agent (id, name) VALUES (2, 'Agent002');

INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (1, 'Ticket 1', 'NEW', '2023-01-01T09:00:00', NULL);
INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (2, 'Ticket 2', 'IN_PROGRESS', CURRENT_TIMESTAMP(), 1);
INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (3, 'Ticket 3', 'NEW', CURRENT_TIMESTAMP(), NULL);
INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (4, 'Ticket 4', 'RESOLVED', CURRENT_TIMESTAMP(), 2);
INSERT INTO ticket (id, description, status, created_date, assigned_agent_id)
VALUES (5, 'Ticket 5', 'RESOLVED', '2023-06-01T09:00:00', 2);
