package com.parking;

import java.time.Duration;

public record Receipt(String ticketId, String plate, Duration duration, long fee) { }
