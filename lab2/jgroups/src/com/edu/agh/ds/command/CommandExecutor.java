package com.edu.agh.ds.command;

import com.edu.agh.ds.command.utils.CommandParser;
import com.edu.agh.ds.command.utils.CommandType;
import com.edu.agh.ds.map.impl.DistributedMap;
import com.edu.agh.ds.utils.Channel;
import org.jgroups.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CommandExecutor {

    private final DistributedMap state;
    private Channel channel;
    private final CommandParser parser = new CommandParser();
    private boolean working = true;

    public CommandExecutor(DistributedMap state, Channel channel) {
        this.state = state;
        this.channel = channel;
    }

    public void execute(String command) {
        Command parsedCommand = parser.parse(command);
        Class<?> c = this.getClass();
        String methodName = getMethodName(parsedCommand.getType());
        try {
            Method method = c.getDeclaredMethod(methodName, Command.class);
            method.invoke(this, parsedCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DistributedMap getState() {
        return state;
    }

    private void executeGet(Command parsedCommand) {
        System.out.println(state.get(parsedCommand.getKey()));
    }

    private void executeShow(Command parsedCommand) {
        System.out.println(state.toString());
    }

    private void executeContains(Command parsedCommand) {
        System.out.println(state.containsKey(parsedCommand.getKey()));
    }

    private void executePut(Command parsedCommand) {
        String op = "put " + parsedCommand.getKey() + " " + parsedCommand.getValue();
        state.put(parsedCommand.getKey(), parsedCommand.getValue());
        try {
            channel.send(new Message(null, null, op));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeRemove(Command parsedCommand) {
        String op = "remove " + parsedCommand.getKey();
        state.remove(parsedCommand.getKey());
        try {
            channel.send(new Message(null, null, op));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeAlternativePut(Command parsedCommand) {
        state.put(parsedCommand.getKey(), parsedCommand.getValue());
    }

    private void executeAlternativeRemove(Command parsedCommand) {
        state.remove(parsedCommand.getKey());
    }

    private void executeEmpty(Command parsedCommand) {
        System.out.println("Empty command!");
    }

    private void executeUnrecognized(Command parsedCommand) {
        System.out.println("Unrecognized command!");
    }

    private void executeQuit(Command parsedCommand) {
        this.working = false;
    }

    private String getMethodName(CommandType type) {
        String typeText = type.getText();
        return "execute"
                + typeText.substring(0, 1).toUpperCase()
                + typeText.substring(1);
    }

    public boolean isWorking() {
        return working;
    }
}
