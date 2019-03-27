package com.edu.agh.ds.command.utils;

import com.edu.agh.ds.command.Command;

public class CommandParser {

    public Command parse(String command) {
        String[] args = command.split("\\s+");
        if(args.length == 0){
            return new Command(CommandType.EMPTY);
        } else if(args.length == 1) {
            return new Command(CommandType.fromString(args[0]));
        } else if(args.length == 2) {
            return new Command(CommandType.fromString(args[0]), args[1]);
        } else if(args.length == 3){
            return new Command(CommandType.fromString(args[0]), args[1], Integer.parseInt(args[2]));
        } else {
            return new Command(CommandType.UNRECOGNIZED);
        }
    }
}
