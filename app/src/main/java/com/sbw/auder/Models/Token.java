package com.sbw.auder.Models;

public class Token {

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String authToken) {
        Token.authToken = authToken;
    }

    public static String authToken;

}
