package com.edu.agh.ds.test;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SimpleChat extends ReceiverAdapter {

    private final List<String> state = new LinkedList<>();
    private JChannel channel;
    private String userName = System.getProperty("user.name", "n/a");

    public static void main(String... args) throws Exception {
        new SimpleChat().start();
    }

    public void viewAccepted(View view){
        System.out.println("** view: " + view);
    }

    public void receive(Message msg) {

        String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
        synchronized (state) {
            state.add(line);
        }
    }

    public void getState(OutputStream outputStream) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(outputStream));
        }
    }

    public void setState(InputStream inputStream) throws Exception {
        List<String> list = (List<String>) Util.objectFromStream(new DataInputStream(inputStream));
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println(list.size() + " messages in chat history.");
        for(String str: list) {
            System.out.println(str);
        }
    }

    private void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("ChatCluster");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    private void eventLoop(){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> ");
                String line = br.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit"))
                    break;

                line = "[" + userName + "] " + line;
                Message msg = new Message(null, null, line);
                channel.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
