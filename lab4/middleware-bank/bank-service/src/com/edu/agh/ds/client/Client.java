package com.edu.agh.ds.client;
// **********************************************************************
//
// Copyright (c) 2003-2016 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

import BankingSystem.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Exception;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import com.zeroc.Ice.*;

public class Client
{
    public static void main(String[] args)
    {
        int status = 0;
        Communicator communicator = null;

        try {
            communicator = Util.initialize(args);

            ObjectPrx base = communicator.stringToProxy("factory/accountFactory:tcp -h " +
                    "localhost -p " +
                    "10000:udp -h localhost -p 10000");

            AccountFactoryPrx factory = AccountFactoryPrx.checkedCast(base);
            if (factory == null) throw new Error("Invalid proxy");

            AccountFactoryPrx factoryOneway = (AccountFactoryPrx)factory.ice_oneway();
            AccountFactoryPrx factoryBatchOneway = (AccountFactoryPrx)factory.ice_batchOneway();
            AccountFactoryPrx factoryDatagram = (AccountFactoryPrx)factory.ice_datagram();
            AccountFactoryPrx factoryBatchDatagram = (AccountFactoryPrx)factory.ice_batchDatagram();

            AccountPrx accountPrx = null;

            String line = null;
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            CompletableFuture<Long> cfl = null;
            do
            {
                try
                {
                    System.out.print("==> ");
                    System.out.flush();
                    line = in.readLine();
                    if (line == null)
                    {
                        break;
                    }
                    else if(line.equals("register")){
                        register(factory, in);
                    } else if(line.equals("login")){
                        accountPrx = getAccountPrx(factory, in);
                    } else if(line.equals("balance")){
                        balance(accountPrx);
                    } else if(line.equals("credit")){
                        credit(accountPrx, in);
                    } else if(line.equals("logout")) {
                        accountPrx = logout(accountPrx);
                    }
                }
                catch (java.io.IOException ex)
                {
                    System.err.println(ex);
                }
            }
            while (!line.equals("x"));


        } catch (LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;
        }
        if (communicator != null) {
            // Clean up
            //
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }

    private static AccountPrx logout(AccountPrx accountPrx) {
        if(accountPrx == null){
            System.out.println("Sorry, you must be logged in to do that.");
        } else {
            System.out.println("Succesfully logged out!");
            accountPrx = null;
        }
        return accountPrx;
    }

    private static void credit(AccountPrx accountPrx, BufferedReader in) throws IOException {
        if(accountPrx == null){
            System.out.println("Sorry, you must be logged in to do that.");
        } else {
            if(accountPrx.ice_isA("::BankingSystem::AccountStandard")){
                System.out.println("Sorry, you must be premium user in to do that" +
                        ".");
            } else {

                Money creditValue = new Money();
                String years;
                System.out.print("Years = ");
                years = in.readLine();
                System.out.print("Value = ");
                creditValue.amount = Double.parseDouble(in.readLine());
                System.out.print("Currency = ");
                creditValue.currency = in.readLine();

                LoanDetails loanDetails =
                        AccountPremiumPrx.checkedCast(accountPrx).applyForLoan(creditValue, years);

                System.out.println("Your credit details: " + loanDetails.toString());
            }
        }
    }

    private static void balance(AccountPrx accountPrx) {
        if(accountPrx == null){
            System.out.println("Sorry, you must be logged in to do that.");
        } else {
            System.out.println("Your balance is: " + accountPrx.getBalance());
        }
    }

    private static AccountPrx getAccountPrx(AccountFactoryPrx factory, BufferedReader in)
            throws IOException {
        AccountPrx accountPrx;
        String pesel;
        String password;
        System.out.print("Pesel = ");
        pesel = in.readLine();
        System.out.print("Password = ");
        password = in.readLine();

        Current current = new Current();
        current.ctx = new HashMap<>();
        current.ctx.put("pwd", password);

        accountPrx = factory.findAccount(pesel, current.ctx);

        if(accountPrx == null) {
            System.out.println("Wrong credentials!");
        }
        else if(accountPrx.ice_isA("::BankingSystem::AccountStandard")){
            System.out.println("Welcome " + pesel + " Standard User!");
        } else {
            System.out.println("Welcome " + pesel + " Premium User!");
        }
        return accountPrx;
    }

    private static void register(AccountFactoryPrx factory, BufferedReader in) throws IOException {
        String name;
        Money income = new Money();
        String pesel;
        String password;
        System.out.print("Name = ");
        name = in.readLine();
        System.out.print("Pesel = ");
        pesel = in.readLine();
        System.out.print("Balance = ");
        income.amount = Double.parseDouble(in.readLine());
        System.out.print("Currency = ");
        income.currency = in.readLine();

        password = factory.createAccount(name, pesel, income);

        System.out.println("Your password is: " + password);
    }

}