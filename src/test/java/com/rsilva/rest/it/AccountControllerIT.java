package com.rsilva.rest.it;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rsilva.rest.AppBinder;
import com.rsilva.rest.controller.AccountController;

@ExtendWith(MockitoExtension.class)
public class AccountControllerIT extends JerseyTest{

	@Override
	protected Application configure() {
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);
		ResourceConfig resourceConfig = new ResourceConfig(AccountController.class);
		resourceConfig.register(new AppBinder());
		return resourceConfig;
	}

	@Test
	public void testCreateAccount_Success() throws Exception {
		Response actual = target("/accounts").request()
				.post(Entity.json(String.class));

		assertThat(actual.getStatus(), is(Status.CREATED.getStatusCode()));
		assertThat(actual.readEntity(String.class), is(not(emptyString())));
	}

	@Test
	public void testGetAccountBalance_ValidAccount_Success() throws Exception {
		String validAccountId = target("/accounts").request().post(Entity.json(String.class), String.class);
		Response actual = target("accounts/" + validAccountId).request().get();

		assertThat(actual.getStatus(), is(Status.OK.getStatusCode()));
	}

	@Test
	public void testGetAccountBalance_InvalidAccount_Fails() throws Exception {
		Response actual = target("accounts/INVALID_ACCOUNT").request().get();
		assertThat(actual.getStatus(), is(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
	}

}
