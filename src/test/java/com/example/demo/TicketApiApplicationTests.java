package com.example.demo;

import com.example.demo.dto.TicketDto;
import com.example.demo.model.Agent;
import com.example.demo.model.Status;
import com.example.demo.model.Ticket;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.TicketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/integrationTestData.sql")
@Tag("integration")
class TicketApiApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private AgentRepository agentRepository;

	@AfterEach
	void tearDown() {
		ticketRepository.deleteAll();
		agentRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createTicket_Successful() {
		TicketDto ticketDto = new TicketDto(null, "Sample Ticket", null, null, null, null, null);

		webTestClient.post().uri("/tickets")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(ticketDto)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> {
					assertNotNull(ticketDtoResponse.id());
					assertEquals("Sample Ticket", ticketDtoResponse.description());
					assertEquals(Status.NEW, ticketDtoResponse.status());
				});
	}

	@Test
	void createTicket_MissingDescription() {
		TicketDto ticketDto = new TicketDto(null, null, null, null, null, null, null);

		webTestClient.post().uri("/tickets")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(ticketDto)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void assignAgentToTicket_Sucesssful() {
		// Given a ticket in "NEW" state
		webTestClient.put().uri("/tickets/100/agent/1")
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> {
					assertEquals(Status.IN_PROGRESS, ticketDtoResponse.status());
					assertEquals("Agent001", ticketDtoResponse.assignedAgent());
				});
	}

	@Test
	void assignAgentToTicket_AlreadyInProgress() {
		// Given a ticket in the "IN_PROGRESS" state
		webTestClient.put().uri("/tickets/101/agent/1")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void resolveTicket_Sucesssful() {
		// Given a ticket in the "IN_PROGRESS" state
		webTestClient.put().uri("/tickets/101/resolve")
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> {
					assertEquals(Status.RESOLVED, ticketDtoResponse.status());
				});
	}

	@Test
	void resolveTicket_InvalidState() {
		// Given a ticket in "NEW" state
		webTestClient.put().uri("/tickets/100/resolve")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void closeTicket_Sucesssful() {
		// Given a ticket in "RESOLVED" status with a resolution summary
		webTestClient.put().uri("/tickets/103/close")
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> {
					assertEquals(Status.CLOSED, ticketDtoResponse.status());
				});
	}

	@Test
	void closeTicket_MissingResolutionSummary() {
		// Given a ticket in "RESOLVED" status without a resolution summary
		webTestClient.put().uri("/tickets/102/close")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void closeTicket_InvalidState() {
		// Given a ticket in "NEW" status
		webTestClient.put().uri("/tickets/100/close")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void updateTicket_Sucesssful() {
		String description = "Updated description";
		// Given a ticket not closed
		TicketDto ticketDto = new TicketDto(100L, description, null, null, null, null, null);

		webTestClient.put().uri("/tickets/100")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(ticketDto)
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> {
					assertEquals(description, ticketDtoResponse.description());
				});
	}

	@Test
	void updateTicket_InvalidState() {
		String description = "Updated description";
		// Given a closed ticket and try to change its state in a way that's not allowed by business rules
		TicketDto ticketDto = new TicketDto(100L, description, null, null, null, null, null);

		webTestClient.put().uri("/tickets/104")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(ticketDto)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void getTicketById_Sucesssful() {
		// Given a ticket with an assigned agent
		Long id = 101L;
		webTestClient.get().uri("/tickets/" + id)
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> {
					assertEquals(id, ticketDtoResponse.id().longValue());
					assertEquals("Agent001", ticketDtoResponse.assignedAgent());
				});
	}

	@Test
	void getTicketById_NonExistent() {
		// Given a non-existent ticket
		Long id = 99L;
		webTestClient.get().uri("/tickets/" + id)
				.exchange()
				.expectStatus().isNotFound();
	}


	@Test
	void getTickets_FilterByStatus() {
		// Filtering tickets with status "CLOSED""
		Status status = Status.CLOSED;
		webTestClient.get().uri(uriBuilder -> uriBuilder.path("/tickets")
						.queryParam("status", status.name())
						.build())
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(TicketDto.class)
				.hasSize(1)
				.consumeWith(response -> {
					for (TicketDto ticketDto : response.getResponseBody()) {
						assertEquals(status, ticketDto.status());
					}
				});
	}

	@Test
	void combinedOperationsTest() {
		// Create a ticket
		TicketDto newTicketDto = new TicketDto(null, "Combined op ticket", Status.NEW, null, null, null, null);
		Long createdTicketId = webTestClient.post().uri("/tickets")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(newTicketDto)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(TicketDto.class)
				.returnResult()
				.getResponseBody()
				.id();

		// Assign an agent
		webTestClient.put().uri("/tickets/" + createdTicketId + "/agent/1")
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> assertEquals(Status.IN_PROGRESS, ticketDtoResponse.status()));

		// Resolve ticket
		webTestClient.put().uri("/tickets/" + createdTicketId + "/resolve")
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> assertEquals(Status.RESOLVED, ticketDtoResponse.status()));

		// Update ticket with resolution summary
		TicketDto resolutionDto = new TicketDto(null, null, null, null, null, null, "Summary");
		webTestClient.put().uri("/tickets/" + createdTicketId)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(resolutionDto)
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> assertEquals("Summary", ticketDtoResponse.resolutionSummary()));

		// Close ticket
		webTestClient.put().uri("/tickets/" + createdTicketId + "/close")
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> assertEquals(Status.CLOSED, ticketDtoResponse.status()));
	}

	@Test
	void assignAgent_DatabaseIntegrity() {
		// Given an existing ticket (id=100) and agent (id=1)
		Long ticketId = 100L;
		Agent agent = agentRepository.findById(1L).get();

		// When we assign an agent to the ticket
		webTestClient.put().uri("/tickets/"+ticketId+"/agent/"+agent.getId())
				.exchange()
				.expectStatus().isOk()
				.expectBody(TicketDto.class)
				.value(ticketDtoResponse -> {
					assertEquals(ticketId, ticketDtoResponse.id());
					assertEquals(agent.getName(), ticketDtoResponse.assignedAgent());
				});

		// Now verify that database has set the foreign key correctly
		// Fetch the ticket directly from the repository
		Ticket ticket = ticketRepository.findById(ticketId).get();
		assertEquals(agent.getId(), ticket.getAssignedAgent().getId().longValue());
	}

}
