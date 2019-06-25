package com.rsilva.rest.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rsilva.rest.AppBinder;
import com.rsilva.rest.controller.AccountController;
import com.rsilva.rest.controller.TransferController;
import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.model.TransferRequest;

@ExtendWith(MockitoExtension.class)
public class TransferControllerIT extends JerseyTest {

	@Override
	protected Application configure() {
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);
		ResourceConfig resourceConfig = new ResourceConfig(TransferController.class, AccountController.class);
		resourceConfig.register(new AppBinder());
		return resourceConfig;
	}

	@Test
	public void testGetTransfersByAccount_Success() throws Exception {
		String validAccountId = target("/accounts").request().post(Entity.json(String.class), String.class);
		@SuppressWarnings("rawtypes")
		List actualList = target("transfers/accounts/" + validAccountId).request().get(List.class);

		assertThat(actualList.isEmpty(), is(true));
	}

	@Test
	public void testCreateTransferRequest_Success() throws Exception {
		//create account1
		String validAccountId1 = target("/accounts").request().post(Entity.json(String.class), String.class);
		// top up account 1
		Response topUpResponse = target(String.format("transfers/accounts/%s/amount/%s", validAccountId1, 5)).request().post(Entity.json(String.class));
		assertThat(topUpResponse.getStatus(), is(HttpStatus.OK_200.getStatusCode()));
		//create account2
		String validAccountId2 = target("/accounts").request().post(Entity.json(String.class), String.class);

		TransferRequest request = TransferRequest.builder()
				.originAccountId(validAccountId1)
				.recipientAccountId(validAccountId2)
				.amount(BigDecimal.ONE)
				.build();

		Transaction actual = target("/transfers").request().post(Entity.entity(request, MediaType.APPLICATION_JSON),
				Transaction.class);

		assertThat(actual.getOriginAccountId(), is(validAccountId1));
		assertThat(actual.getRecipientAccountId(), is(validAccountId2));
		assertThat(actual.getAmount().getUnits(), is(BigDecimal.ONE));
	}

	@Test
	public void testCreateTransferRequest_ValidationError() throws Exception {
		//create account1
		String validAccountId1 = target("/accounts").request().post(Entity.json(String.class), String.class);
		//create account2
		String validAccountId2 = target("/accounts").request().post(Entity.json(String.class), String.class);

		TransferRequest request = TransferRequest.builder()
				.originAccountId(validAccountId1)
				.recipientAccountId(validAccountId2)
				.amount(BigDecimal.ONE)
				.build();

		Response response = target("/transfers").request().post(Entity.entity(request, MediaType.APPLICATION_JSON));
		assertThat(response.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode()));
	}

	@Test
	public void testTopUp_Success() throws Exception {
		//create account1
		String validAccountId1 = target("/accounts").request().post(Entity.json(String.class), String.class);

		Response topUpResponse = target(String.format("transfers/accounts/%s/amount/%s", validAccountId1, 5)).request().post(Entity.json(String.class));
		assertThat(topUpResponse.getStatus(), is(HttpStatus.OK_200.getStatusCode()));
	}

	@Test
	public void testTopUp_NegativeAmount_Fails() throws Exception {
		//create account1
		String validAccountId1 = target("/accounts").request().post(Entity.json(String.class), String.class);

		Response topUpResponse = target(String.format("transfers/accounts/%s/amount/%s", validAccountId1, -5)).request().post(Entity.json(String.class));
		assertThat(topUpResponse.getStatus(), is(HttpStatus.BAD_REQUEST_400.getStatusCode()));
	}

	@Test
	public void testTopUp_ValidationError() throws Exception {
		Response topUpResponse = target(String.format("transfers/accounts/%s/amount/%s", "INVALID_ACC", 5)).request().post(Entity.json(String.class));
		assertThat(topUpResponse.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode()));
	}

}
