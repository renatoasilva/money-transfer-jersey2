package com.rsilva.rest.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.TopUpRequest;
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
		TopUpRequest topUprequest = TopUpRequest.builder().originAccountId(validAccountId1).amount(BigDecimal.valueOf(5L)).build();
		Response topUpResponse = target("transfers/top-up").request().post(Entity.json(topUprequest));
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
	public void testCreateTransferRequest_MultipleTransactionsParallel_Success() throws Exception {
		//create account1
		CompletableFuture<String> futureValidAccount1 = CompletableFuture.supplyAsync(() -> target("/accounts").request().post(Entity.json(String.class), String.class));
		//create account2
		CompletableFuture<String> futureValidAccount2 = CompletableFuture.supplyAsync(() -> target("/accounts").request().post(Entity.json(String.class), String.class));
		String account1 = futureValidAccount1.get();
		String account2 = futureValidAccount2.get();

		// top up account 1
		final TopUpRequest topUprequest = TopUpRequest.builder().originAccountId(account1).amount(BigDecimal.valueOf(5L)).build();
		CompletableFuture<Response> topUp1 = CompletableFuture.supplyAsync(() -> target("transfers/top-up").request().post(Entity.json(topUprequest)));

		// top up account 2
		final TopUpRequest topUprequest2 = TopUpRequest.builder().originAccountId(account2).amount(BigDecimal.valueOf(5L)).build();
		CompletableFuture<Response> topUp2 = CompletableFuture.supplyAsync(() -> target("transfers/top-up").request().post(Entity.json(topUprequest2)));

		TransferRequest request1 = TransferRequest.builder()
				.originAccountId(account1)
				.recipientAccountId(account2)
				.amount(BigDecimal.ONE)
				.build();

		TransferRequest request2 = TransferRequest.builder()
				.originAccountId(account2)
				.recipientAccountId(account1)
				.amount(BigDecimal.valueOf(2L))
				.build();

		topUp1.get();

		//acc1 = 5 -1, acc2=5+1
		CompletableFuture<Transaction> transfer1 = CompletableFuture.supplyAsync(()-> target("/transfers").request().post(Entity.entity(request1, MediaType.APPLICATION_JSON),
				Transaction.class));
		//acc1 = 5 -1+2, acc2=5+1-2
		CompletableFuture<Transaction> transfer2 = CompletableFuture.supplyAsync(()-> target("/transfers").request().post(Entity.entity(request2, MediaType.APPLICATION_JSON),
				Transaction.class));
		//acc1 = 5 -1+2-1, acc2=5+1-2+1
		CompletableFuture<Transaction> transfer3 = CompletableFuture.supplyAsync(()-> target("/transfers").request().post(Entity.entity(request1, MediaType.APPLICATION_JSON),
				Transaction.class));
		//acc1 = 5 -1+2-1+2, acc2=5+1-2+1-2
		CompletableFuture<Transaction> transfer4 = CompletableFuture.supplyAsync(()-> target("/transfers").request().post(Entity.entity(request2, MediaType.APPLICATION_JSON),
				Transaction.class));
		//acc1 = 5-1+2-1+2-1=6, acc2=5+1-2+1-2+1=4
		CompletableFuture<Transaction> transfer5 = CompletableFuture.supplyAsync(()-> target("/transfers").request().post(Entity.entity(request1, MediaType.APPLICATION_JSON),
				Transaction.class));

		topUp2.get();
		transfer1.get();
		transfer2.get();
		transfer3.get();
		transfer4.get();
		transfer5.get();

		CompletableFuture<Amount> balanceAccount1 = CompletableFuture.supplyAsync(() -> target("accounts/" + account1).request().get(Amount.class));
		CompletableFuture<Amount> balanceAccount2 = CompletableFuture.supplyAsync(() -> target("accounts/" + account2).request().get(Amount.class));

		assertThat(balanceAccount1.get().getUnits(), is(BigDecimal.valueOf(6L)));
		assertThat(balanceAccount2.get().getUnits(), is(BigDecimal.valueOf(4L)));
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
		TopUpRequest request = TopUpRequest.builder().originAccountId(validAccountId1).amount(BigDecimal.ONE).build();

		Response topUpResponse = target("transfers/top-up").request().post(Entity.json(request));
		assertThat(topUpResponse.getStatus(), is(HttpStatus.OK_200.getStatusCode()));
	}

	@Test
	public void testTopUp_NegativeAmount_Fails() throws Exception {
		//create account1
		String validAccountId1 = target("/accounts").request().post(Entity.json(String.class), String.class);
		TopUpRequest request = TopUpRequest.builder().originAccountId(validAccountId1).amount(BigDecimal.valueOf(-5L)).build();

		Response topUpResponse = target("transfers/top-up").request().post(Entity.json(request));
		assertThat(topUpResponse.getStatus(), is(HttpStatus.BAD_REQUEST_400.getStatusCode()));
	}

	@Test
	public void testTopUp_ValidationError() throws Exception {
		TopUpRequest request = TopUpRequest.builder().originAccountId("INVALID_ACC").amount(BigDecimal.valueOf(5L)).build();
		Response topUpResponse = target("transfers/top-up").request().post(Entity.json(request));

		assertThat(topUpResponse.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode()));
	}

}
