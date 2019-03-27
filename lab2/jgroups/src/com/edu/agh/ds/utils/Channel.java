package com.edu.agh.ds.utils;

import org.jgroups.JChannel;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

import java.net.InetAddress;

public class Channel extends JChannel {

    public Channel() throws Exception {
        super(false);
        this.initStack();
    }


    private void initStack() throws Exception {
        ProtocolStack stack = new ProtocolStack();
        this.setProtocolStack(stack);

        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("230.100.200.114")))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE());

        stack.init();
    }
}
