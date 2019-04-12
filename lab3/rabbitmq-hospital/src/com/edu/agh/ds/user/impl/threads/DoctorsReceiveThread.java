package com.edu.agh.ds.user.impl.threads;

import com.rabbitmq.client.*;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;


public class DoctorsReceiveThread extends Thread {

    private String queueName;
    private Channel channel;
    private String corrId;

    public DoctorsReceiveThread(String queueName, Channel channel, String corrId) {
        this.queueName = queueName;
        this.channel = channel;
        this.corrId = corrId;
    }

    @Override
    public void run() {
        try {
            channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    String message = new String(delivery.getBody(), UTF_8);
                    System.out.println("Received: " + message);
                }
            }, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
