package com.rsilva.rest.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.Error;
import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.model.TransferRequest;
import com.rsilva.rest.repository.AccountRepository.Operation;

import jersey.repackaged.com.google.common.base.Preconditions;
import jersey.repackaged.com.google.common.collect.ImmutableList;

@Singleton
public class TransferServiceImpl implements TransferService {

	@Inject
	private AccountService accountService;

	@Inject
	private TransactionService transactionService;

	@Override
	public List<Transaction> getTransfers(String accountId) {
		return transactionService.getTransactions(accountId);
	}

	@Override
	public Transaction createTransfer(TransferRequest inputRequest) {
		Preconditions.checkArgument(!inputRequest.getOriginAccountId().equals(inputRequest.getRecipientAccountId()), "Origin and recipient accounts must be different");
		Amount amount = Amount.builder().units(inputRequest.getAmount()).build();
		Error error = null;
		Transaction transaction;
		try {
			// validates that the recipient account exists
			accountService.getAccountBalance(inputRequest.getRecipientAccountId());
			accountService.updateAccountBalance(inputRequest.getOriginAccountId(), amount, Operation.DEBIT);
			accountService.updateAccountBalance(inputRequest.getRecipientAccountId(), amount, Operation.CREDIT);
		} catch (Exception exception) {
			error = Error.builder().message(exception.getMessage()).build();
			throw exception;
		} finally {
			transaction = transactionService.addTransaction(inputRequest.getOriginAccountId(),
					inputRequest.getRecipientAccountId(), amount,
					error != null ? ImmutableList.of(error) : Collections.emptyList());
		}

		return transaction;
	}

	@Override
	public Transaction topUp(String accountId, BigDecimal topUpamount) {
		Amount amount = Amount.builder().units(topUpamount).build();
		Error error = null;
		Transaction transaction;
		try {
			// validates that the recipient account exists
			accountService.getAccountBalance(accountId);
			accountService.updateAccountBalance(accountId, amount, Operation.CREDIT);
		} catch (Exception exception) {
			error = Error.builder().message(exception.getMessage()).build();
			throw exception;
		} finally {
			transaction = transactionService.addTransaction(accountId, accountId, amount,
					error != null ? ImmutableList.of(error) : Collections.emptyList());
		}
		return transaction;
	}

}
