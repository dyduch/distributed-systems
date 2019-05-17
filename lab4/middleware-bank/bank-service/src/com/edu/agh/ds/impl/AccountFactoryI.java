package com.edu.agh.ds.impl;

import BankingSystem.*;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.Identity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AccountFactoryI implements AccountFactory {

    Map<String, AccountI> accounts = new HashMap<>();

    @Override
    public String createAccount(String name, String pesel, Money income, Current current) {
        double balanceValue = new Random().nextInt(100000);
        AccountI account;
        String password = null;
        Identity identity;
        if (accounts.get(pesel) == null) {

            if (income.amount > 5000) {
                account = new AccountPremiumI(name, income, pesel, new Money(balanceValue,
                        income.currency));
                identity = new Identity(pesel, "premium");
            } else {
                account = new AccountStandardI(name, income, pesel, new Money(balanceValue,
                        income.currency));
                identity = new Identity(pesel, "standard");
            }
            password = randomString(5);
            account.setPassword(password);
            accounts.put(pesel, account);

            current.adapter.add(account, identity);

        }

        return password;
    }

    @Override
    public AccountPrx findAccount(String pesel, Current current) {
        AccountI account;
        Identity identity;
        String password = current.ctx.get("pwd");
        if(accounts.get(pesel) != null && password.equals(accounts.get(pesel).getPassword())){
            account = accounts.get(pesel);
            if(account instanceof AccountPremiumI){
                identity = new Identity(pesel, "premium");
                return AccountPremiumPrx.uncheckedCast(current.adapter.createProxy(identity));
            } else {
                identity = new Identity(pesel, "standard");
                return AccountStandardPrx.uncheckedCast(current.adapter.createProxy(identity));
            }
        }
        return null;
    }

    private String randomString(int length) {

        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toLowerCase();
        StringBuilder builder = new StringBuilder();
        while (length-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
