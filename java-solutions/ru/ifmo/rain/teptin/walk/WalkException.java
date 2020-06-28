package ru.ifmo.rain.teptin.walk;

class WalkException extends Exception {
    WalkException(String message) {
        super(message);
    }
    WalkException(String message, Exception e) {
        super(e == null ? message : message + "\n" + e.getMessage());
    }
}
