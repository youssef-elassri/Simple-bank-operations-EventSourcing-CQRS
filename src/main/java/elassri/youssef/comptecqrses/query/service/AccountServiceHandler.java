package elassri.youssef.comptecqrses.query.service;

import elassri.youssef.comptecqrses.commonApi.enums.AccountStatus;
import elassri.youssef.comptecqrses.commonApi.enums.OperationType;
import elassri.youssef.comptecqrses.commonApi.events.AccountActivatedEvent;
import elassri.youssef.comptecqrses.commonApi.events.AccountCreatedEvent;
import elassri.youssef.comptecqrses.commonApi.events.AccountCreditedEvent;
import elassri.youssef.comptecqrses.commonApi.events.AccountDebitedEvent;
import elassri.youssef.comptecqrses.commonApi.queries.GetAccountByIdQuery;
import elassri.youssef.comptecqrses.commonApi.queries.GetAllAccountsQuery;
import elassri.youssef.comptecqrses.query.entities.Account;
import elassri.youssef.comptecqrses.query.entities.Operation;
import elassri.youssef.comptecqrses.query.repositories.AccountRepository;
import elassri.youssef.comptecqrses.query.repositories.OperationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class AccountServiceHandler {
    private AccountRepository accountRepository;
    private OperationRepository operationRepository;

    @EventHandler
    public void on(AccountCreatedEvent event){
        log.info("********************");
        log.info("Account created event received");
        accountRepository.save(new Account(
                event.getId(),
                event.getInitialBalance(),
                event.getStatus(),
                event.getCurrency(),
                null
        ));
    }

    @EventHandler
    public void on(AccountActivatedEvent event){
        log.info("********************");
        log.info("Account activated event received");
        Account account=accountRepository.findById(event.getId()).get();
        account.setStatus(event.getStatus());
    }

    @EventHandler
    public void on(AccountCreditedEvent event){
        log.info("********************");
        log.info("Credit event received");
        Account account=accountRepository.findById(event.getId()).get();
        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setAmount(event.getAmount());
        operation.setDate(new Date());
        operation.setType(OperationType.CREDIT);
        operationRepository.save(operation);
        account.setBalance(account.getBalance() + event.getAmount());
    }

    @EventHandler
    public void on(AccountDebitedEvent event){
        log.info("********************");
        log.info("Credit activated event received");
        Account account=accountRepository.findById(event.getId()).get();

        Operation operation = new Operation();
        operation.setAccount(account);
        operation.setAmount(event.getAmount());
        operation.setDate(new Date());
        operation.setType(OperationType.DEBIT);
        operationRepository.save(operation);
        account.setBalance(account.getBalance() - event.getAmount());
    }

    @QueryHandler
    public List<Account> on(GetAllAccountsQuery query){
        return accountRepository.findAll();
    }

    @QueryHandler
    public Account on(GetAccountByIdQuery query){
        return accountRepository.getById(query.getId());
    }
}
