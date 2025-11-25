package org.example;

public class AppContext {
    private static String jwtToken;

    public static void setJwtToken(String token) {
        jwtToken = token;
    }

    public static String getJwtToken() {
        return jwtToken;
    }
}
