package com.edu.agh.ds.user.impl;

import com.edu.agh.ds.user.User;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class Admin implements User {

    private static final String ADMIN_EXCHANGE = "admin_exchange";


    private Connection connection;
    private Channel channel;
    private String queueName;

    public Admin() {
        this.setup();
    }

    @Override
    public void run() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println(
                        getDate(System.currentTimeMillis()) + " [" + properties.getUserId() + "] " +
                                " " + message);
            }
        };

        channel.basicConsume(queueName, true, consumer);

    }

    @Override
    public void setup() {
        System.out.println(this.getClass().getSimpleName().toUpperCase());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            channel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.FANOUT);
            queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ADMIN_EXCHANGE, "");

            System.out.println(this.getClass().getSimpleName() + " initialised\nWaiting for " +
                    "messages...");

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }


    private String getDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(time);
        return sdf.format(resultdate);
    }
}
