package com.rsilva.rest.repository;

import com.rsilva.rest.exception.AccountNotFoundException;
import com.rsilva.rest.model.Amount;

public interface AccountRepository {

	public enum Operation{
		CREDIT,
		DEBIT
	}

	/**
	 * Create a new account
	 *
	 * @return the accountId
	 */
	String createAccount();

	/**
	 * Get account balance if account exists.  
	 * Otherwise {@link AccountNotFoundException} will be thrown
	 * 
	 * @param accountId
	 * @return the {@link Amount} associate to this account
	 */
	Amount getAccountBalance(String accountId);

	void updateAccountBalance(String accountId, Amount transactionAmount, Operation operation);

}
