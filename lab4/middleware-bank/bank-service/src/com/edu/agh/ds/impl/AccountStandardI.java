package com.edu.agh.ds.impl;

import BankingSystem.AccountStandard;
import BankingSystem.Money;
import com.zeroc.Ice.Current;

public class AccountStandardI extends AccountI implements AccountStandard {

    public AccountStandardI(String name, Money income, String pesel,
                            Money balance) {
        super(name, income, pesel, balance);
    }
}
