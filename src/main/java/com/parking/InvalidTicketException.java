package com.parking;

public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) { super(message); }
}
