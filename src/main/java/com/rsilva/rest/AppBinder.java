package com.rsilva.rest;

import javax.inject.Singleton;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.rsilva.rest.repository.AccountRepository;
import com.rsilva.rest.repository.AccountRepositoryImpl;
import com.rsilva.rest.repository.TransactionRepository;
import com.rsilva.rest.repository.TransactionRepositoryImpl;
import com.rsilva.rest.service.AccountService;
import com.rsilva.rest.service.AccountServiceImpl;
import com.rsilva.rest.service.TransactionService;
import com.rsilva.rest.service.TransactionServiceImpl;
import com.rsilva.rest.service.TransferService;
import com.rsilva.rest.service.TransferServiceImpl;

public class AppBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(AccountServiceImpl.class).to(AccountService.class).in(Singleton.class);
		bind(TransactionServiceImpl.class).to(TransactionService.class).in(Singleton.class);
		bind(TransferServiceImpl.class).to(TransferService.class).in(Singleton.class);
		bind(AccountRepositoryImpl.class).to(AccountRepository.class).in(Singleton.class);
		bind(TransactionRepositoryImpl.class).to(TransactionRepository.class).in(Singleton.class);
	}
}
