package com.rsilva.rest.model;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequest {

	@NotBlank
	private String originAccountId;

	@NotNull
	@Min(value = 1)
	private BigDecimal amount;
}
