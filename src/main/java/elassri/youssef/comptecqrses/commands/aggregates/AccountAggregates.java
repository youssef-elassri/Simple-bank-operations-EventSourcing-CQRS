package elassri.youssef.comptecqrses.commands.aggregates;

import elassri.youssef.comptecqrses.commonApi.commands.CreateAccountCommand;
import elassri.youssef.comptecqrses.commonApi.commands.CreditAccountCommand;
import elassri.youssef.comptecqrses.commonApi.commands.DebitAccountCommand;
import elassri.youssef.comptecqrses.commonApi.enums.AccountStatus;
import elassri.youssef.comptecqrses.commonApi.events.AccountActivatedEvent;
import elassri.youssef.comptecqrses.commonApi.events.AccountCreatedEvent;
import elassri.youssef.comptecqrses.commonApi.events.AccountCreditedEvent;
import elassri.youssef.comptecqrses.commonApi.events.AccountDebitedEvent;
import elassri.youssef.comptecqrses.commonApi.exceptions.AmountNegativeException;
import elassri.youssef.comptecqrses.commonApi.exceptions.BalanceNotSufficientException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class AccountAggregates {
    @AggregateIdentifier
    private String accountId;
    private double balance;
    private String currency;
    private AccountStatus status;

    public AccountAggregates() {
    }
    @CommandHandler
    public AccountAggregates(CreateAccountCommand createAccountCommand) {
        if(createAccountCommand.getInitialBalance()<0)
            throw new RuntimeException("Initial Balance Cannot Be Negative");
        //Ok
        AggregateLifecycle.apply(new AccountCreatedEvent(
                createAccountCommand.getId(),
                createAccountCommand.getInitialBalance(),
                createAccountCommand.getCurrency(),
                AccountStatus.CREATED));
    }
    @EventSourcingHandler
    public void on(AccountCreatedEvent event){
        this.accountId = event.getId();
        this.balance = event.getInitialBalance();
        this.currency= event.getCurrency();
        this.status=AccountStatus.CREATED;
        AggregateLifecycle.apply(new AccountActivatedEvent(
                event.getId(),
                AccountStatus.ACTIVATED
        ));
    }
    @EventSourcingHandler
    public void on(AccountActivatedEvent event){
        this.status=event.getStatus();
    }

    @CommandHandler
    public void handle(CreditAccountCommand command){
        if(command.getAmount() < 0) throw new AmountNegativeException("Amount should not be negative");
        AggregateLifecycle.apply(new AccountCreditedEvent(
                command.getId(),
                command.getAmount(),
                command.getCurrency()
        ));
    }

    @EventSourcingHandler
    public void on(AccountCreditedEvent event){
        this.balance += event.getAmount();
    }

    @CommandHandler
    public void handle(DebitAccountCommand command){
        if(command.getAmount() < 0) throw new AmountNegativeException("Amount should not be negative");
        if(this.balance < command.getAmount()) throw new BalanceNotSufficientException("Balance Insufficient"+"=>"+this.balance);
        AggregateLifecycle.apply(new AccountDebitedEvent(
                command.getId(),
                command.getAmount(),
                command.getCurrency()
        ));
    }

    @EventSourcingHandler
    public void on(AccountDebitedEvent event){
        this.balance -= event.getAmount();
    }

}
