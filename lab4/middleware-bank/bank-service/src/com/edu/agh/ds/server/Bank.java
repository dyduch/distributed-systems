package com.edu.agh.ds.server;

import com.edu.agh.ds.impl.AccountFactoryI;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import com.zeroc.IceInternal.Ex;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import sr.grpc.gen.*;

import java.util.HashMap;

public class Bank {

    private final ManagedChannel channel;
    private final ExchangeRateServiceGrpc.ExchangeRateServiceStub exchangeRateServiceStub;

    private final HashMap<Currency, Double> exchangeRateValue = new HashMap<>();

    public Bank(String host, int exchangeRateServicePort) {
        channel = ManagedChannelBuilder.forAddress(host, exchangeRateServicePort)
                .usePlaintext(true)
                .build();

        exchangeRateServiceStub = ExchangeRateServiceGrpc.newStub(channel);
    }

    public void start(String[] args)
    {
        int status = 0;
        Communicator communicator = null;

        try
        {
            exchangeRateValue.put(Currency.GBP, 1.0);
            exchangeRateValue.put(Currency.USD, 1.0);
            exchangeRateValue.put(Currency.CHF, 1.0);

            getExchangeRates();

            communicator = Util.initialize(args);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("BankAdapter",
                    "tcp " +
                    "-h localhost -p 10000:udp -h localhost -p 10000");

            AccountFactoryI factoryServant = new AccountFactoryI();
            adapter.add(factoryServant, new Identity("accountFactory", "factory"));

            adapter.activate();

            System.out.println("Entering event processing loop...");

            communicator.waitForShutdown();

        }
        catch (Exception e)
        {
            System.err.println(e);
            status = 1;
        }
        if (communicator != null)
        {
            // Clean up
            //
            try
            {
                communicator.destroy();
            }
            catch (Exception e)
            {
                System.err.println(e);
                status = 1;
            }
        }
        System.exit(status);
    }


    public static void main(String[] args)
    {
        Bank bank = new Bank("localhost", 21370);
        bank.start(args);
    }

    private void getExchangeRates() {
        exchangeRateServiceStub.getExchangeRates(
                ExchangeRateRq.newBuilder().addAllCurrencies(exchangeRateValue.keySet()).setDomesticCurrency(Currency.EUR).build(),
                new StreamObserver<ExchangeRateRs>() {
                    @Override
                    public void onNext(ExchangeRateRs exchangeRateRs) {
                        exchangeRateValue.replace(exchangeRateRs.getExchangeRate().getCurrency(),
                                exchangeRateRs.getExchangeRate().getRate());
                        System.out.println(exchangeRateValue);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println(throwable);

                    }

                    @Override
                    public void onCompleted() {
                        System.out.println(exchangeRateValue);
                    }
                });
    }

}
