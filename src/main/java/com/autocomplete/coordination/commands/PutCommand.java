package com.autocomplete.coordination.commands;

import io.atomix.copycat.Command;

public class PutCommand implements Command<Boolean> {
    private final String string;
    private final Integer weight;

    public PutCommand(String string, Integer weight) {
        this.string = string;
        this.weight = weight;
    }

    public String getString() {
        return this.string;
    }

    public Integer getWeight() {
        return this.weight;
    }
}
