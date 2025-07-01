package com.example.frontend_chat.utils;

public class Validation {

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            ShowDialogs.showWarningDialog("Not a valid email, please try again.");
            return false;
        }
        return true;
    }

    public static boolean isValidPassword(String password) {
        if (password != null && password.length() >= 6) {
            return true;
        }
        ShowDialogs.showWarningDialog("Password must be at least 6 characters.");
        return false;
    }

    public static boolean isValidPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty() || !phoneNumber.matches("^\\+?\\d{10,15}$")) {
            ShowDialogs.showWarningDialog("Not a valid phone number, please try again.");
            return false;
        }
        return true;
    }

    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            ShowDialogs.showWarningDialog("Name cannot be empty.");
            return false;
        }
        return true;
    }

    public static boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            ShowDialogs.showWarningDialog("Address cannot be empty.");
            return false;
        }
        return true;
    }
    // Simple URL validation (can be enhanced as needed)
    public static boolean isValidUrl(String url) {
        return url.matches("^(https?|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    }

    public static boolean validateToken() {
        String token = TokenManager.getInstance().getToken();
        System.out.println("Token: " + token);
        if (token == null || token.isEmpty()) {
            return false;
        }
        return true;
    }
}
