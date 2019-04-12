package com.edu.agh.ds.user.impl.threads;

import com.rabbitmq.client.*;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TechniciansReceiveThread extends Thread {

    private String queueName;
    private Channel channel;
    private DeliverCallback deliverCallback;

    public TechniciansReceiveThread(String queueName, Channel channel) {
        this.queueName = queueName;
        this.channel = channel;
        this.deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String message = "";
            try{
                message = new String(delivery.getBody(), UTF_8);
                System.out.println("Received: " + message);
                message += " [DONE]";
                sleep(2000);
            } catch (RuntimeException | InterruptedException e) {
                System.out.println(" [.] " + e.toString());
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), props, message.getBytes(UTF_8));
                System.out.println("Sent: " + message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
    }

    @Override
    public void run() {
        try {
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

