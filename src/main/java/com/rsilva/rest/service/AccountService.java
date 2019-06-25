package com.rsilva.rest.service;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.repository.AccountRepository.Operation;

public interface AccountService {

	String create();

	Amount getAccountBalance(String accountId);

	void updateAccountBalance(String accountId, Amount transactionAmount, Operation operation);


}
