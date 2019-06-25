package com.rsilva.rest.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsilva.rest.exception.AccountNotFoundException;
import com.rsilva.rest.exception.InsufficientFundsException;
import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.model.TransferRequest;
import com.rsilva.rest.service.TransferService;

import jersey.repackaged.com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
public class TransferControllerTest extends JerseyTest {
	private static final String ACCOUNT_ID = "accountId";
	private static final String ACCOUNT_ID2 = "accountId2";

	private ObjectMapper objectMapper = new ObjectMapper();

	private List<Transaction> listTransfers;

	@Mock
	private TransferService transferService;

	@InjectMocks
	private TransferController transferController;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		listTransfers = ImmutableList.of(Transaction.builder().build());

		when(transferService.getTransfers(ACCOUNT_ID)).thenReturn(listTransfers);
		when(transferService.getTransfers(ACCOUNT_ID2)).thenReturn(Collections.emptyList());
		when(transferService.createTransfer(any(TransferRequest.class))).thenReturn(listTransfers.get(0));
	}

	@Override
	protected Application configure() {
		setup();
		ResourceConfig resourceConfig = new ResourceConfig(TransferController.class);

		resourceConfig.register(new AbstractBinder() {
			protected void configure() {
				bind(transferController).to(TransferController.class);
				bind(InsufficientFundsException.class);
			}
		});

		return resourceConfig;
	}

	@Test
	public void testGetTransfersByAccount_Success() throws Exception {
		@SuppressWarnings("rawtypes")
		List responseBody = target("transfers/accounts/" + ACCOUNT_ID).request().get(List.class);

		String actual = objectMapper.writeValueAsString(responseBody);
		String expected = objectMapper.writeValueAsString(listTransfers);
		assertThat(actual, is(expected));
	}

	@Test
	public void testGetTransfersByAccount_ZeroTransfers_Success() throws Exception {
		@SuppressWarnings("rawtypes")
		List actual = target("transfers/accounts/" + ACCOUNT_ID2).request().get(List.class);

		assertThat(actual, is(Collections.emptyList()));
	}

	@Test
	public void testCreateTransferRequest_Success() throws Exception {
		TransferRequest request = TransferRequest.builder().originAccountId(ACCOUNT_ID).recipientAccountId(ACCOUNT_ID2)
				.amount(BigDecimal.TEN).build();

		Transaction actual = target("/transfers").request().post(Entity.entity(request, MediaType.APPLICATION_JSON),
				Transaction.class);

		assertThat(actual, is(listTransfers.get(0)));
		verify(transferService).createTransfer(request);
	}

	@Test
	public void testCreateTransferRequest_ValidationError() throws Exception {
		when(transferService.createTransfer(any(TransferRequest.class)))
				.thenThrow(new InsufficientFundsException(ACCOUNT_ID));
		TransferRequest request = TransferRequest.builder().originAccountId(ACCOUNT_ID).recipientAccountId(ACCOUNT_ID2)
				.amount(BigDecimal.TEN).build();

		Response response = target("/transfers").request().post(Entity.entity(request, MediaType.APPLICATION_JSON));
		assertThat(response.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode()));
		verify(transferService).createTransfer(request);
	}

	@Test
	public void testTopUp_Success() throws Exception {
		when(transferService.topUp(any(String.class), any(BigDecimal.class))).thenReturn(listTransfers.get(0));

		Transaction actual = target(String.format("transfers/accounts/%s/amount/%s", ACCOUNT_ID, 10)).request()
				.post(Entity.json(String.class), Transaction.class);

		assertThat(actual, is(listTransfers.get(0)));
		verify(transferService).topUp(ACCOUNT_ID, BigDecimal.TEN);
	}

	@Test
	public void testTopUp_AccountNotFoundValidationError() throws Exception {
		when(transferService.topUp(any(String.class), any(BigDecimal.class)))
				.thenThrow(new AccountNotFoundException(ACCOUNT_ID));

		Response response = target(String.format("transfers/accounts/%s/amount/%s", ACCOUNT_ID, 10)).request()
				.post(Entity.json(String.class));

		assertThat(response.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode()));
		verify(transferService).topUp(ACCOUNT_ID, BigDecimal.TEN);
	}

	@Test
	public void testTopUp_ZeroAmountError() throws Exception {
		Response response = target(String.format("transfers/accounts/%s/amount/%s", ACCOUNT_ID, 0)).request()
				.post(Entity.json(String.class));

		assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST_400.getStatusCode()));
		verifyNoMoreInteractions(transferService);
	}

}
