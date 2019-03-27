package com.edu.agh.ds.command;

import com.edu.agh.ds.map.impl.DistributedMap;
import com.edu.agh.ds.utils.Channel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class CommandReceiver extends ReceiverAdapter {

    private final DistributedMap state;
    private Channel channel;
    private CommandExecutor executor;

    public CommandReceiver(DistributedMap state, Channel channel) {
        this.state = state;
        this.channel = channel;
        this.executor = new CommandExecutor(state, channel);
    }

    @Override
    public void receive(Message msg) {
        System.out.println(msg.getSrc() + ": " + msg.getObject());
        executor.execute("alt_" + msg.getObject());
    }

    @Override
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state.getHashMap(), new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        Map<String, Integer> hashMap;
        hashMap = (Map<String, Integer>) Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.setState(hashMap);
        }
    }
}
