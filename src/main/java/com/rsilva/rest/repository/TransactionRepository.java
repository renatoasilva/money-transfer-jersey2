package com.rsilva.rest.repository;

import java.util.List;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.Error;
import com.rsilva.rest.model.Transaction;

public interface TransactionRepository {

	/**
	 * Returns a list of transactions associated to this account.
	 *
	 * @param accountId
	 * @return a potential empty list of transactions
	 */
	List<Transaction> getTransactions(String accountId);

//	/**
//	 * Transfers balance from originAccountId to recipientAccountId for the amount specified.
//	 * Accounts and funds are validated, throwing {@link AccountNotFoundException} or {@link InsufficientFundsException}.
//	 * 
//	 * @param originAccountId
//	 * @param recipientAccountId
//	 * @param amount
//	 * @return the resulting transaction.
//	 */
//	Transaction transferBalance(String originAccountId, String recipientAccountId, Amount amount);

	Transaction addTransaction(String originAccountId, String recipientAccountId, Amount amount, List<Error> errors);


}
