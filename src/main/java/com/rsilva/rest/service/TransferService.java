package com.rsilva.rest.service;

import java.math.BigDecimal;
import java.util.List;

import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.model.TransferRequest;

public interface TransferService {

	/**
	 * Return list of transactions for this account 
	 * @return potential empty list of transactions
	 */
	List<Transaction> getTransfers(String accountId);

	Transaction createTransfer(TransferRequest inputRequest);

	Transaction topUp(String accountId, BigDecimal amount);

}
