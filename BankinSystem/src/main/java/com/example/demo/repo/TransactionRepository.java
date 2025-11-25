package com.example.demo.repo;




import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.model.Transaction;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findBySourceAccountOrDestinationAccount(String source, String dest);
    List<Transaction> findBySourceAccount(String source);
}
