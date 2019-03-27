package com.edu.agh.ds.command;

import com.edu.agh.ds.command.utils.CommandType;

public class Command {

    private CommandType type;
    private String key;
    private Integer value;

    public Command(CommandType type, String key, Integer value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public Command(CommandType type, String key) {
        this.type = type;
        this.key = key;
    }

    public Command(CommandType type) {
        this.type = type;
    }

    public CommandType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }
}
