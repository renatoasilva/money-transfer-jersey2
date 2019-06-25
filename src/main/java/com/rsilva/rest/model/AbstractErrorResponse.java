package com.rsilva.rest.model;

import java.util.List;

import lombok.Data;

@Data
public abstract class AbstractErrorResponse {

	private Boolean sucess;
	private List<Error> errors;
}
