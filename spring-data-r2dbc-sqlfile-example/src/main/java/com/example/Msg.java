package com.example;

import org.springframework.data.annotation.Id;

public final class Msg {

    @Id
    private final Long id;
    private final String txt;

    public Msg(final Long id, final String txt) {
        this.id = id;
        this.txt = txt;
    }

    public static Msg create(final String txt) {
        return new Msg(null, txt);
    }

    public Msg withId(final Long id) {
        return new Msg(id, txt);
    }

    public Msg withTxt(final String txt) {
        return new Msg(id, txt);
    }

    public Long getId() {
        return id;
    }

    public String getTxt() {
        return txt;
    }

    @Override
    public String toString() {
        return String.format("Msg(%s, %s)", id, txt);
    }
}
