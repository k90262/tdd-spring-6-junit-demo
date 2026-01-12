package com.example.demo;

import com.example.demo.dto.TicketDto;
import com.example.demo.model.Status;
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
		// Given a ticket in "NEW" state
		webTestClient.put().uri("/tickets/101/agent/1")
				.exchange()
				.expectStatus().isBadRequest();
	}
}
