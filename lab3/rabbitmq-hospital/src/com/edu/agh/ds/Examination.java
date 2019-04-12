package com.edu.agh.ds;

public enum Examination {
    HIP("hip"),
    KNEE("knee"),
    ELBOW("elbow"),
    WRONG("wrong");

    private String text;

    Examination(String text){
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static Examination fromString(String text){
        for (Examination type : Examination.values()) {
            if(type.text.equalsIgnoreCase(text) && !text.equalsIgnoreCase("wrong"))
                return type;
        }
        return WRONG;
    }

}
