package com.edu.agh.ds.command;

import com.edu.agh.ds.map.impl.DistributedMap;
import com.edu.agh.ds.utils.Channel;
import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Vector;

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
        executor.execute("alternative" + msg.getObject());
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("** view: " + view);
        handleView(channel, view);
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

    private static void handleView(Channel channel, View new_view) {
        if(new_view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(channel, (MergeView)new_view);
            handler.start();
        }
    }

    private static class ViewHandler extends Thread {
        Channel channel;
        MergeView view;

        private ViewHandler(Channel channel, MergeView view) {
            this.channel = channel;
            this.view = view;
        }

        public void run() {
            Vector<View> subgroups = (Vector<View>) view.getSubgroups();
            View firstElement = subgroups.firstElement();
            Address localAddress = channel.getAddress();
            if (!firstElement.getMembers().contains(localAddress)) {
                System.out.println("Not member of the new primary partition ("
                        + firstElement + "), will re-acquire the state");
                try {
                    channel.getState(null, 30000);
                } catch (Exception ex) {
                }
            } else {
                System.out.println("Not member of the new primary partition ("
                        + firstElement + "), will do nothing");
            }
        }
    }
}
