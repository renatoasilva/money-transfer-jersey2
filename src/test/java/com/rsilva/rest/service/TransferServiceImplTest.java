package com.rsilva.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rsilva.rest.exception.AccountNotFoundException;
import com.rsilva.rest.exception.InsufficientFundsException;
import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.Error;
import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.model.TransferRequest;
import com.rsilva.rest.repository.AccountRepository.Operation;

import jersey.repackaged.com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
public class TransferServiceImplTest {
	private static final String VALID_ACCOUNT_1 = "VALID_ACCOUNT_1";
	private static final String VALID_ACCOUNT_2 = "VALID_ACCOUNT_2";
	private static final String INVALID_ACCOUNT_ID = "INVALID_ACCOUNT_ID";

	@Mock
	private AccountService accountService;

	@Mock
	private TransactionService transactionService;

	@Mock
	private Transaction newTransaction;

	@Captor
	private ArgumentCaptor<List<Error>> errorsCaptor;

	@InjectMocks
	private TransferServiceImpl underTest;

	@Test
	public void testGetTransfers_Success() throws Exception {
		Transaction transaction = Transaction.builder().id("ID").originAccountId(VALID_ACCOUNT_1)
				.recipientAccountId(VALID_ACCOUNT_2).amount(Amount.builder().units(BigDecimal.TEN).build()).build();
		when(transactionService.getTransactions(VALID_ACCOUNT_1)).thenReturn(ImmutableList.of(transaction));

		assertThat(underTest.getTransfers(VALID_ACCOUNT_1), is(ImmutableList.of(transaction)));
	}

	@Test
	public void testCreateTransfer_Success() throws Exception {
		when(transactionService.addTransaction(VALID_ACCOUNT_1, VALID_ACCOUNT_2,
				Amount.builder().units(BigDecimal.TEN).build(), Collections.emptyList())).thenReturn(newTransaction);
		TransferRequest inputRequest = TransferRequest.builder().originAccountId(VALID_ACCOUNT_1)
				.recipientAccountId(VALID_ACCOUNT_2).amount(BigDecimal.TEN).build();

		Transaction actual = underTest.createTransfer(inputRequest);
		assertThat(actual, is(newTransaction));
	}

	@Test
	public void testCreateTransfer_InvalidOriginAccount_Fails() throws Exception {
		Mockito.doThrow(new AccountNotFoundException(INVALID_ACCOUNT_ID)).when(accountService)
				.updateAccountBalance(eq(INVALID_ACCOUNT_ID), any(), any());

		TransferRequest inputRequest = TransferRequest.builder().originAccountId(INVALID_ACCOUNT_ID)
				.recipientAccountId(VALID_ACCOUNT_2).amount(BigDecimal.TEN).build();

		Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.createTransfer(inputRequest));
		assertTransaction(INVALID_ACCOUNT_ID, VALID_ACCOUNT_2, Amount.builder().units(inputRequest.getAmount()).build(),
				"'INVALID_ACCOUNT_ID' is an invalid account. Please provide a valid account.");
	}

	@Test
	public void testCreateTransfer_InvalidRecipientAccount_Fails() throws Exception {
		Mockito.doThrow(new AccountNotFoundException(INVALID_ACCOUNT_ID)).when(accountService)
				.getAccountBalance(INVALID_ACCOUNT_ID);

		TransferRequest inputRequest = TransferRequest.builder().originAccountId(VALID_ACCOUNT_1)
				.recipientAccountId(INVALID_ACCOUNT_ID).amount(BigDecimal.TEN).build();

		Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.createTransfer(inputRequest));
		assertTransaction(VALID_ACCOUNT_1, INVALID_ACCOUNT_ID, Amount.builder().units(inputRequest.getAmount()).build(),
				"'INVALID_ACCOUNT_ID' is an invalid account. Please provide a valid account.");
	}

	@Test
	public void testCreateTransfer_InsufficientFunds_Fails() throws Exception {
		Mockito.doThrow(new InsufficientFundsException(INVALID_ACCOUNT_ID)).when(accountService).updateAccountBalance(
				eq(INVALID_ACCOUNT_ID), eq(Amount.builder().units(BigDecimal.TEN).build()), eq(Operation.DEBIT));

		TransferRequest inputRequest = TransferRequest.builder().originAccountId(INVALID_ACCOUNT_ID)
				.recipientAccountId(VALID_ACCOUNT_2).amount(BigDecimal.TEN).build();

		Assertions.assertThrows(InsufficientFundsException.class, () -> underTest.createTransfer(inputRequest));
		assertTransaction(INVALID_ACCOUNT_ID, VALID_ACCOUNT_2, Amount.builder().units(inputRequest.getAmount()).build(),
				"Account INVALID_ACCOUNT_ID has insufficient funds to complete the transfer.");

	}

	@Test
	public void testCreateTransfer_OriginRecipientAccountsTheSame_Fails() throws Exception {		
		TransferRequest inputRequest = TransferRequest.builder().originAccountId(VALID_ACCOUNT_2)
				.recipientAccountId(VALID_ACCOUNT_2).amount(BigDecimal.TEN).build();

		Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createTransfer(inputRequest));
	}

	private void assertTransaction(String originAccountId, String recipientAccountId, Amount amount,
			String expectedErrorMessage) {
		verify(transactionService).addTransaction(eq(originAccountId), eq(recipientAccountId), eq(amount),
				errorsCaptor.capture());
		List<Error> errors = errorsCaptor.getValue();
		assertEquals(1, errors.size());
		assertThat(errors.get(0).getMessage(), is(expectedErrorMessage));
	}

	@Test
	public void testTopUp_Success() throws Exception {
		when(transactionService.addTransaction(VALID_ACCOUNT_1, VALID_ACCOUNT_1,
				Amount.builder().units(BigDecimal.TEN).build(), Collections.emptyList())).thenReturn(newTransaction);

		Transaction actual = underTest.topUp(VALID_ACCOUNT_1, BigDecimal.TEN);
		assertThat(actual, is(newTransaction));
	}

	@Test
	public void testTopUp_ValidationError_Fails() throws Exception {
		when(accountService.getAccountBalance(INVALID_ACCOUNT_ID)).thenThrow(new AccountNotFoundException(INVALID_ACCOUNT_ID));

		Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.topUp(INVALID_ACCOUNT_ID, BigDecimal.TEN));
	}

}
