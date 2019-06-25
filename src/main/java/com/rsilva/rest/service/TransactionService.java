package com.rsilva.rest.service;

import java.util.List;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.Error;
import com.rsilva.rest.model.Transaction;

public interface TransactionService {

	List<Transaction> getTransactions(String accountId);

	Transaction addTransaction(String originAccountId, String recipientAccountId, Amount amount, List<Error> error);

}
