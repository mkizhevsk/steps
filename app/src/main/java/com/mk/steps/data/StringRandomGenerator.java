package com.mk.steps.data;

import java.util.Random;

public class StringRandomGenerator {

    public synchronized static StringRandomGenerator getInstance() {
        if(_instance == null)
            _instance = new StringRandomGenerator();
        return _instance;
    }

    private static StringRandomGenerator _instance = null;

    private Random rng = new Random();
    private String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String generateString(int length) {
        char[] text= new char[length];
        for(int i = 0; i < length; i++)
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        return new String(text);
    }

    public String getValue() {
        return generateString(14);
    }
}
