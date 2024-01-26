package org.dreamwork.tools.sherpa.wrapper.examples.utils;

public class MessageWrapper {
    String pattern, name, type;
    Object[] args;

    MessageWrapper (String pattern, Object... args) {
        this.pattern = pattern;
        this.args = args;
        this.name = Thread.currentThread ().getName ();
    }
}