package com.api.bankApi.services.impl;

import com.api.bankApi.exceptions.SystemException;
import com.api.bankApi.models.NewTransactionDto;
import com.api.bankApi.repositories.BankAccountRepository;
import com.api.bankApi.repositories.TransactionRepository;
import com.api.bankApi.services.ITransactionService;
import com.api.bankApi.utils.Limits;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author martin
 */
@Service
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, BankAccountRepository bankAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public Mono<BigDecimal> getBalance() {
        return bankAccountRepository.getAccountBalance().publishOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> depositFunds(NewTransactionDto transaction) {
        return verifyTransactionLimits(transaction, Limits.DEPOSIT)
                .filter(verified -> verified.equalsIgnoreCase("verified"))
                .flatMap(save -> transactionRepository.saveTransaction("DEPOSIT", transaction.amount(), transaction.reference()))
                .map(resp -> checkDbResponse(resp, "DEPOSIT"));
    }

    @Override
    public Mono<String> withdrawFunds(NewTransactionDto transaction) {
        return bankAccountRepository.getAccountBalance()
                .publishOn(Schedulers.boundedElastic())
                .filter(bal -> bal.compareTo(transaction.amount()) > 0)
                .switchIfEmpty(Mono.error(new SystemException("Insufficient funds.")))
                .flatMap(verify -> verifyTransactionLimits(transaction, Limits.WITHDRAWAL))
                .filter(verified -> verified.equalsIgnoreCase("verified"))
                .flatMap(save -> transactionRepository.saveTransaction("WITHDRAW", transaction.amount(), transaction.reference()))
                .map(resp -> checkDbResponse(resp, "WITHDRAW"));
    }

    private Mono<String> verifyTransactionLimits(NewTransactionDto transaction, Limits txn_type) {
        return transactionRepository.getAccountlimits(txn_type.toString())
                .publishOn(Schedulers.boundedElastic())
                .map(acc_lim -> {
                    var max_txn_amt = BigDecimal.valueOf(txn_type.valueOfmaxTransactionAmount());
                    var max_daily_amt = BigDecimal.valueOf(txn_type.valueOfmaxDailyAmount());
                    var max_txn_count = txn_type.valueOfmaxTransactions();
                    if (transaction.amount().compareTo(max_txn_amt) > 0) {
                        throw new SystemException(String.format("Exceeded maximum %s per transaction", txn_type.toString().toLowerCase()));
                    }
                    if (acc_lim.total_transactions().add(transaction.amount()).compareTo(max_daily_amt) > 0) {
                        throw new SystemException(String.format("Exceeded maximum total %s amounts per day", txn_type.toString().toLowerCase()));
                    }
                    
                    if(transaction.amount().compareTo(BigDecimal.ZERO) < 0) {
                        throw new SystemException("Negative amounts not allowed");
                    }

                    if (acc_lim.frequency() >= max_txn_count) {
                        throw new SystemException(String.format("Exceeded total number of %ss allowed per day", txn_type.toString().toLowerCase()));
                    }
                    return "verified";
                });

    }
    
    private String checkDbResponse(String response,String txn_type) {
        if (!response.equalsIgnoreCase("success")) {
            throw new SystemException(response);
        }
        
        return txn_type.equalsIgnoreCase("DEPOSIT") ? "Funds deposited successfully." : "Funds Withdrawn successfully";
    }

}
