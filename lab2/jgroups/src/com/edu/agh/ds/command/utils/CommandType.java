package com.edu.agh.ds.command.utils;

public enum CommandType {
    PUT("put"),
    ALT_PUT("alt_put"),
    GET("get"),
    REMOVE("remove"),
    ALT_REMOVE("alt_remove"),
    CONTAINS("contains"),
    SHOW("show"),
    QUIT("quit");

    private String text;

    CommandType(String text){
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static CommandType fromString(String text){
        for (CommandType type : CommandType.values()) {
            if(type.text.equalsIgnoreCase(text))
                return type;
        }
        return null;
    }
}
