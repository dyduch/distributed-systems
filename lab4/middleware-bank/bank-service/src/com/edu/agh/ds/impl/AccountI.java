package com.edu.agh.ds.impl;

import BankingSystem.Account;
import BankingSystem.Money;
import com.zeroc.Ice.Current;

public class AccountI implements Account {

    private String name;
    private Money income;
    private String pesel;
    private Money balance;
    private String password;

    public AccountI(String name, Money income, String pesel, Money balance) {
        this.name = name;
        this.income = income;
        this.pesel = pesel;
        this.balance = balance;
    }

    @Override
    public double getBalance(Current current) {
        return this.balance.amount;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public Money getIncome() {
        return income;
    }

    public String getPesel() {
        return pesel;
    }

    public Money getBalance() {
        return balance;
    }

    public String getPassword() {
        return password;
    }
}
