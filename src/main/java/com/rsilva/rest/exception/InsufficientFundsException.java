package com.rsilva.rest.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Provider
public class InsufficientFundsException extends RuntimeException implements ExceptionMapper<InsufficientFundsException> {

	private static final long serialVersionUID = -4926890727903249758L;
	private static final String DEFAULT_MESSAGE = "Account %s has insufficient funds to complete the transfer.";

	public InsufficientFundsException(String accountId) {
		super(String.format(DEFAULT_MESSAGE, accountId));
	}

	@Override
	public Response toResponse(InsufficientFundsException exception) {
		return Response.status(Status.EXPECTATION_FAILED).entity(exception.getMessage()).type(MediaType.APPLICATION_JSON)
				.build();

	}
}
