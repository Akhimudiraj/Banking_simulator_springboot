package com.example.demo.test;

import com.example.demo.exceptions.AccountNotFoundException;
import com.example.demo.exceptions.InsufficientBalanceException;
import com.example.demo.exceptions.InvalidAmountException;
import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.repo.AccountRepository;
import com.example.demo.repo.TransactionRepository;
import com.example.demo.service.AccountService;
import com.example.demo.service.IdGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository txnRepo;

    @Mock
    private IdGenerator idGen;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        account = new Account();
        account.setAccountNumber("ACC1234");
        account.setHolderName("Harish");
        account.setBalance(1000.0);
        account.setStatus("ACTIVE");
    }

    @Test
    void testCreateAccount() {
        when(idGen.generateAccountNumber("Harish")).thenReturn("ACC1234");
        when(accountRepo.existsByAccountNumber("ACC1234")).thenReturn(false);
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account created = accountService.createAccount("Harish");

        assertNotNull(created);
        assertEquals("ACC1234", created.getAccountNumber());
        verify(accountRepo, times(1)).save(any(Account.class));
    }

    @Test
    void testDeposit() {
        when(accountRepo.findByAccountNumber("ACC1234")).thenReturn(Optional.of(account));
        when(idGen.generateTransactionId()).thenReturn("TXN001");
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction txn = accountService.deposit("ACC1234", 500.0);

        assertNotNull(txn);
        assertEquals("ACC1234", txn.getDestinationAccount());
        assertEquals(1500.0, account.getBalance(), 0.001);
    }

    @Test
    void testWithdraw() {
        when(accountRepo.findByAccountNumber("ACC1234")).thenReturn(Optional.of(account));
        when(idGen.generateTransactionId()).thenReturn("TXN002");
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction txn = accountService.withdraw("ACC1234", 400.0);

        assertNotNull(txn);
        assertEquals("ACC1234", txn.getSourceAccount());
        assertEquals(600.0, account.getBalance(), 0.001);
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        when(accountRepo.findByAccountNumber("ACC1234")).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class,
                () -> accountService.withdraw("ACC1234", 2000.0));
    }

    @Test
    void testDeposit_InvalidAmount() {
        when(accountRepo.findByAccountNumber("ACC1234")).thenReturn(Optional.of(account));
        assertThrows(InvalidAmountException.class,
                () -> accountService.deposit("ACC1234", -100.0));
    }

    @Test
    void testTransfer() {
        Account dest = new Account();
        dest.setAccountNumber("ACC5678");
        dest.setHolderName("Alice");
        dest.setBalance(500.0);

        when(accountRepo.findByAccountNumber("ACC1234")).thenReturn(Optional.of(account));
        when(accountRepo.findByAccountNumber("ACC5678")).thenReturn(Optional.of(dest));
        when(idGen.generateTransactionId()).thenReturn("TXN003");
        when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(txnRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction txn = accountService.transfer("ACC1234", "ACC5678", 300.0);

        assertEquals(700.0, account.getBalance(), 0.001);
        assertEquals(800.0, dest.getBalance(), 0.001);
        assertEquals("ACC1234", txn.getSourceAccount());
        assertEquals("ACC5678", txn.getDestinationAccount());
    }

    @Test
    void testGetByAccountNumber_NotFound() {
        when(accountRepo.findByAccountNumber("XYZ")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.getByAccountNumber("XYZ"));
    }

    @Test
    void testGetTransactions() {
        Transaction txn = new Transaction();
        txn.setTransactionId("TXN004");

        when(accountRepo.findByAccountNumber("ACC1234")).thenReturn(Optional.of(account));
        when(txnRepo.findBySourceAccountOrDestinationAccount("ACC1234", "ACC1234"))
                .thenReturn(Collections.singletonList(txn));

        List<Transaction> txns = accountService.getTransactionsFor("ACC1234");

        assertEquals(1, txns.size());
        assertEquals("TXN004", txns.get(0).getTransactionId());
    }
}
