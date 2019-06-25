package com.rsilva.rest.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.model.TransferRequest;
import com.rsilva.rest.service.TransferService;

@Path("transfers")
public class TransferController {

	@Inject
	private TransferService transferService;

	@GET
	@Path("/accounts/{accountId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Transaction> getTransfersByAccount(@NotBlank @PathParam("accountId") String accountId) {
		return transferService.getTransfers(accountId);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Transaction createTransferRequest(@NotNull @Valid TransferRequest inputRequest) {
		return transferService.createTransfer(inputRequest);
	}

	@POST
	@Path("/accounts/{accountId}/amount/{amount}")
	@Produces(MediaType.APPLICATION_JSON)
	public Transaction topUp(@NotBlank @PathParam("accountId") String accountId, @NotNull @Min(1) @PathParam("amount") BigDecimal amount) {
		return transferService.topUp(accountId, amount);
	}

}
