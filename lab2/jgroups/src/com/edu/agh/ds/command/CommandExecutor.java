package com.edu.agh.ds.command;

import com.edu.agh.ds.command.utils.CommandParser;
import com.edu.agh.ds.map.impl.DistributedMap;
import com.edu.agh.ds.utils.Channel;
import org.jgroups.Message;

public class CommandExecutor {

    private final DistributedMap state;
    private Channel channel;
    private final CommandParser parser = new CommandParser();

    public CommandExecutor(DistributedMap state, Channel channel) {
        this.state = state;
        this.channel = channel;
    }

    public void execute(String command) {
        Command parsedCommand = parser.parse(command);
        switch (parsedCommand.getType()) {
            case GET:
                executeGet(parsedCommand);
                break;
            case SHOW:
                executeShow(parsedCommand);
                break;
            case CONTAINS:
                executeContains(parsedCommand);
                break;
            case PUT:
                executePut(parsedCommand);
                break;
            case REMOVE:
                executeRemove(parsedCommand);
                break;
            case ALT_PUT:
                executeAlternativePut(parsedCommand);
                break;
            case ALT_REMOVE:
                executeAlternativeRemove(parsedCommand);
                break;
            default:
                break;
        }
    }

    public DistributedMap getState() {
        return state;
    }

    private void executeGet(Command parsedCommand) {
        System.out.println(state.get(parsedCommand.getKey()));
    }

    private void executeShow(Command parsedCommand) {
        System.out.println(state);
    }

    private void executeContains(Command parsedCommand) {
        System.out.println(state.containsKey(parsedCommand.getKey()));
    }

    private void executePut(Command parsedCommand) {
        String op = "put" + parsedCommand.getKey() + parsedCommand.getValue();
        state.put(parsedCommand.getKey(), parsedCommand.getValue());
        try {
            channel.send(new Message(null, null, op));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeRemove(Command parsedCommand) {
        String op = "remove" + parsedCommand.getKey();
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

}
