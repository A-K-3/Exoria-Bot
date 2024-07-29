package com.ak;

import java.util.concurrent.ThreadLocalRandom;

public class UserGenerator {
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomUser() {
        return java.util.stream.IntStream.range(0, ThreadLocalRandom.current().nextInt(3, 17))
                .mapToObj(i -> String.valueOf(ALPHANUMERIC.charAt(ThreadLocalRandom.current().nextInt(ALPHANUMERIC.length()))))
                .collect(java.util.stream.Collectors.joining());
    }
}