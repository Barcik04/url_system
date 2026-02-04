package com.example.url_system.utils.tokenhashing;


import java.security.SecureRandom;

public final class TokenGenerator {
    private static final SecureRandom RND = new SecureRandom();
    private static final char[] ALPH = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private TokenGenerator() {}

    public static String randomToken(int len) {
        char[] out = new char[len];
        for (int i = 0; i < len; i++) out[i] = ALPH[RND.nextInt(ALPH.length)];
        return new String(out);
    }
}

