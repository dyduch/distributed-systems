package com.edu.agh.ds;

import com.edu.agh.ds.command.CommandExecutor;
import com.edu.agh.ds.command.CommandReceiver;
import com.edu.agh.ds.map.impl.DistributedMap;
import com.edu.agh.ds.utils.Channel;
import org.jgroups.ReceiverAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HashMapApp extends ReceiverAdapter {

    private final DistributedMap state = new DistributedMap();
    private CommandExecutor executor;
    private Channel channel;
    private CommandReceiver receiver;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        new HashMapApp().start();
    }

    private void start() throws Exception {
        channel = new Channel();
        receiver = new CommandReceiver(state, channel);
        executor = new CommandExecutor(state, channel);
        channel.setReceiver(receiver);
        channel.connect("HashMapCluster");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }


    private void eventLoop() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (executor.isWorking()) {
            try {
                String command = br.readLine().toLowerCase();
                executor.execute(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
