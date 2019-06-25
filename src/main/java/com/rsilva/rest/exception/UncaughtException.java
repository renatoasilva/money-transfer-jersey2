package com.rsilva.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ParamException;

@Provider
public class UncaughtException extends Throwable implements ExceptionMapper<Throwable> {

	private static final long serialVersionUID = -9056482648117851861L;

	@Override
	public Response toResponse(Throwable exception) {
		if(exception instanceof ParamException) {
			return ((ParamException) exception).getResponse();
		}
		return Response.status(500).entity("Ooops. Our robots got a bit crazy right now. Please try again !!")
				.type("text/plain").build();
	}
}
