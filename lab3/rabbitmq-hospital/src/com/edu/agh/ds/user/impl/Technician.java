package com.edu.agh.ds.user.impl;

import com.edu.agh.ds.user.User;
import com.rabbitmq.client.*;
;import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Technician implements User {

    private static final String DOC_EXCHANGE_NAME = "doc_exchange";

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
        channel.exchangeDeclare(DOC_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, DOC_EXCHANGE_NAME, firstType);
        channel.queueBind(queueName, DOC_EXCHANGE_NAME, secondType);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Received: " + message);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        System.out.println("Waiting for messages...");
        channel.basicQos(1);
        channel.basicConsume(queueName, true, consumer);
    }


    @Override
    public void setup() {
        System.out.println(this.getClass().getSimpleName().toUpperCase());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            System.out.println("Enter type: ");
            firstType = br.readLine();
            System.out.println("Enter type: ");
            secondType = br.readLine();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }
}
