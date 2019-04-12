package com.edu.agh.ds.user.impl;

import com.edu.agh.ds.user.User;
import com.edu.agh.ds.user.impl.threads.DoctorsReceiveThread;
import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Doctor implements User, AutoCloseable {

    private static final String TECH_EXCHANGE = "tech_exchange";

    private static String QUEUE;

    private Connection connection;
    private Channel channel;

    public Doctor() {
        this.setup();
    }

    @Override
    public void run() {

        final String corrId = UUID.randomUUID().toString();
        DoctorsReceiveThread thread = new DoctorsReceiveThread(QUEUE, channel, corrId);
        thread.run();

            try {
                AMQP.BasicProperties props = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(corrId)
                        .replyTo(QUEUE)
                        .build();

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                while(true) {
                    System.out.println("Enter patient's name: ");
                    String name = br.readLine();
                    System.out.println("Enter medical type: ");
                    String type = br.readLine().toLowerCase();

                    String message = name + " " + type;

                    channel.basicPublish(TECH_EXCHANGE, type, props, message.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Sent: " + message);
                }

            } catch (IOException e) {
                e.printStackTrace();
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
            channel.exchangeDeclare(TECH_EXCHANGE, BuiltinExchangeType.DIRECT);
            QUEUE = channel.queueDeclare().getQueue();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
