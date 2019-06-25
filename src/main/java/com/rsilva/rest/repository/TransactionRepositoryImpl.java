package com.rsilva.rest.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.Error;
import com.rsilva.rest.model.Transaction;

public class TransactionRepositoryImpl implements TransactionRepository {

	private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

	@Override
	public List<Transaction> getTransactions(String accountId) {
		return transactions.stream()
				.filter(transaction -> accountId.equals(transaction.getOriginAccountId()) || 
						accountId.equals(transaction.getRecipientAccountId()))
				.collect(Collectors.toList());
	}

	@Override
	public Transaction addTransaction(String originAccountId, String recipientAccountId, Amount amount, List<Error> errors) {
		Transaction newTransaction = Transaction.builder().originAccountId(originAccountId)
				.recipientAccountId(recipientAccountId).amount(amount)
				.build();

		newTransaction.setErrors(errors);
		newTransaction.setSucess(newTransaction.getErrors().isEmpty());
		transactions.add(newTransaction);
		return newTransaction;
	}

}
