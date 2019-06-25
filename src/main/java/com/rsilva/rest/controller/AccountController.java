package com.rsilva.rest.controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.service.AccountService;


@Path("accounts")
public class AccountController {

	@Inject
	private AccountService accountService;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAccount() {
		return Response.status(javax.ws.rs.core.Response.Status.CREATED).entity(accountService.create()).build();
	}

	@GET
	@Path("/{accountId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Amount getAccountBalance(@PathParam("accountId") String accountId) {
		return accountService.getAccountBalance(accountId);
	}

}
