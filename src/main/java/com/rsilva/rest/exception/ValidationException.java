package com.rsilva.rest.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationException extends RuntimeException implements ExceptionMapper<IllegalArgumentException> {

	private static final long serialVersionUID = 6250450664017598694L;

	@Override
	public Response toResponse(IllegalArgumentException exception) {
		return Response.status(Status.BAD_REQUEST).entity(exception.getMessage()).type(MediaType.APPLICATION_JSON)
				.build();
	}

}
