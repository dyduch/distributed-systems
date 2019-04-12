package com.edu.agh.ds.user.impl;

import com.edu.agh.ds.Examination;
import com.edu.agh.ds.user.User;
import com.edu.agh.ds.user.impl.threads.TechniciansReceiveThread;
import com.rabbitmq.client.*;
;import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Technician implements User {

    private static final String TECH_EXCHANGE = "tech_exchange";

    private String firstType;
    private String secondType;

    private Connection connection;
    private Channel channel;

    public Technician(){
        this.setup();
    }

    public Technician(String... types) {
        this.firstType = types[0];
        this.secondType = types[1];
        this.setup();
    }

    @Override
    public void run() throws IOException {

        TechniciansReceiveThread firstTypeThread =
                new TechniciansReceiveThread(firstType + "_queue", channel);
        TechniciansReceiveThread secondTypeThread =
                new TechniciansReceiveThread(secondType + "_queue", channel);

        channel.queueDeclare(firstType + "_queue", true, false, false, null);
        channel.queueBind(firstType + "_queue", TECH_EXCHANGE, firstType);
        channel.queueDeclare(secondType + "_queue", true, false, false, null);
        channel.queueBind(secondType + "_queue", TECH_EXCHANGE, secondType);

        channel.basicQos(1);

        firstTypeThread.run();
        secondTypeThread.run();
    }


    @Override
    public void setup() {
        System.out.println(this.getClass().getSimpleName().toUpperCase());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            channel.exchangeDeclare(TECH_EXCHANGE, BuiltinExchangeType.DIRECT);

            setFirstType(br);
            setSecondType(br);

            System.out.println(this.getClass().getSimpleName() + " initialised\nWaiting fro messages...");

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    private void setFirstType(BufferedReader br) throws IOException {
        firstType = enterType(br);
    }

    private void setSecondType(BufferedReader br) throws IOException {
        secondType = enterType(br);
    }

    private String enterType(BufferedReader br) throws IOException {
        System.out.println("Enter type: ");
        String type = br.readLine().toLowerCase();
        while(Examination.fromString(type).equals(Examination.WRONG)){
            System.out.println("Wrong type, re-enter type: ");
            type = br.readLine().toLowerCase();
        }
        return type;
    }
}
