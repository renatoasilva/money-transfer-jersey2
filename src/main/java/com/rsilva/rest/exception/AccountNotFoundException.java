package com.rsilva.rest.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.NoArgsConstructor;

@Provider
@NoArgsConstructor
public class AccountNotFoundException extends RuntimeException implements ExceptionMapper<AccountNotFoundException> {

	private static final long serialVersionUID = 1069261338954993431L;
	private static final String DEFAULT_MESSAGE = "'%s' is an invalid account. Please provide a valid account.";

	public AccountNotFoundException(String accountId) {
		super(String.format(DEFAULT_MESSAGE, accountId));
	}

	public AccountNotFoundException(Throwable exception) {
		super(DEFAULT_MESSAGE, exception);
	}

	@Override
	public Response toResponse(AccountNotFoundException exception) {
		return Response.status(Status.BAD_REQUEST).entity(exception.getMessage()).type(MediaType.APPLICATION_JSON)
				.build();
	}

}
