package com.example.biscuit.sessionStorage;

public class Session {
    private static String email;
    private static int passwordLength;

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        Session.email = email;
    }

    public static int getPasswordLength() {
        return passwordLength;
    }

    public static void setPasswordLength(int passwordLength) {
        Session.passwordLength = passwordLength;
    }
}
