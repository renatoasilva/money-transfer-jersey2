package com.rsilva.rest.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.Error;
import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.repository.TransactionRepository;

@Singleton
public class TransactionServiceImpl implements TransactionService {

	@Inject
	private TransactionRepository transactionRepository;

	@Override
	public List<Transaction> getTransactions(String accountId) {
		return transactionRepository.getTransactions(accountId);
	}

	@Override
	public Transaction addTransaction(String originAccountId, String recipientAccountId, Amount amount,
			List<Error> errors) {
		return transactionRepository.addTransaction(originAccountId, recipientAccountId, amount, errors);
	}

}
