package com.edu.agh.ds.user.impl;

import com.edu.agh.ds.user.User;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Doctor implements User {

    private static final String DOC_EXCHANGE_NAME = "doc_exchange";

    private Connection connection;
    private Channel channel;

    public Doctor() {
        this.setup();
    }

    @Override
    public void run() {
        while (true) {
            try {
                channel.exchangeDeclare(DOC_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter name: ");
                String name = br.readLine();
                System.out.println("Enter type: ");
                String type = br.readLine();

                String message = name + " " + type;

                channel.basicPublish(DOC_EXCHANGE_NAME, type, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("Sent: " + message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setup() {
        System.out.println(this.getClass().getSimpleName().toUpperCase());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }


    }
}
