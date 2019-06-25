package com.rsilva.rest.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
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

import com.rsilva.rest.exception.AccountNotFoundException;
import com.rsilva.rest.model.Amount;
import com.rsilva.rest.service.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest extends JerseyTest {
	private static final String ACCOUNT_ID = "accountId";
	private static final String INVALID_ACCOUNT_ID = "InvalidAccountId";

	private Amount amount = Amount.builder().units(BigDecimal.ONE).build();

	@Mock
	private AccountService accountService;

	@InjectMocks
	private AccountController accountController;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(accountService.create()).thenReturn("NEW_ACCOUNT_ID");

		when(accountService.getAccountBalance(ACCOUNT_ID)).thenReturn(amount);
		when(accountService.getAccountBalance(INVALID_ACCOUNT_ID)).thenThrow(new AccountNotFoundException(INVALID_ACCOUNT_ID));
	}

	@Override
	protected Application configure() {
		setup();
		ResourceConfig resourceConfig = new ResourceConfig(AccountController.class);

		resourceConfig.register(new AbstractBinder() {
			protected void configure() {
				bind(accountController).to(AccountController.class);
			}
		});

		return resourceConfig;
	}

	@Test
	public void testCreateAccount_Success() throws Exception {
		String actual = target("accounts").request().post(Entity.json(String.class), String.class);

		assertThat(actual, is("NEW_ACCOUNT_ID"));
	}

	@Test
	public void testGetAccountBalance_Success() throws Exception {
		final Amount actual = target("accounts/" + ACCOUNT_ID).request().get(Amount.class);

		assertThat(actual, is(amount));
		verify(accountService).getAccountBalance(ACCOUNT_ID);
	}

	@Test
	public void testGetAccountBalance_AccountNotFound_Fails() throws Exception {
		Response response = target("accounts/" + INVALID_ACCOUNT_ID).request().get();
		assertThat(response.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode()));

		verify(accountService).getAccountBalance(INVALID_ACCOUNT_ID);
	}

}
