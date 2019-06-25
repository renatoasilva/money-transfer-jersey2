package com.rsilva.rest.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rsilva.rest.model.Amount;
import com.rsilva.rest.model.Transaction;
import com.rsilva.rest.model.Error;

@ExtendWith(MockitoExtension.class)
public class TransactionRepositoryImplTest {
	private static final String ACCOUNT_ID = "accountId";

	@Mock
	private Transaction newTransaction;
	@Spy
	private List<Transaction> transactions = new ArrayList<>();
	@InjectMocks
	private TransactionRepositoryImpl underTest;

	@BeforeEach
	private void setup() {
		transactions.add(newTransaction);
	}

	@Test
	public void testGetTransactions_Success() throws Exception {
		when(newTransaction.getOriginAccountId()).thenReturn(ACCOUNT_ID);

		assertThat(underTest.getTransactions(ACCOUNT_ID), is(Collections.singletonList(newTransaction)));
	}

	@Test
	public void testGetTransactions_UnknowAccountId_Success() throws Exception {
		assertThat(underTest.getTransactions("Unknown"), IsEmptyCollection.empty());
	}

	@Test
	public void testAddTransaction_Success() throws Exception {
		List<Error> errors = Collections.singletonList(Error.builder().message("ooops").build());
		Amount amount = Amount.builder().units(BigDecimal.ONE).build();

		Transaction actual = underTest.addTransaction(ACCOUNT_ID, "recipientAccountId", amount, errors);
		assertThat(actual.getAmount(), is(amount));
		assertThat(actual.getOriginAccountId(), is(ACCOUNT_ID));
		assertThat(actual.getRecipientAccountId(), is("recipientAccountId"));
		assertThat(actual.getErrors(), is(errors));
	}

}
