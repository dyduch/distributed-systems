package com.edu.agh.ds.impl;

import BankingSystem.AccountPremium;
import BankingSystem.Currency;
import BankingSystem.LoanDetails;
import BankingSystem.Money;
import com.zeroc.Ice.Current;

public class AccountPremiumI extends AccountI implements AccountPremium {

    public AccountPremiumI(String name, Money income, String pesel, Money balance) {
        super(name, income, pesel, balance);
    }

    @Override
    public LoanDetails applyForLoan(Money value, String loanTime, Current current) {
        LoanDetails loanDetails = new LoanDetails();
        loanDetails.loanTime = loanTime;
        loanDetails.domesticValue = new Money(value.amount/ 3, "EUR");
        loanDetails.foreignValue = value;

        return loanDetails;
    }
}
