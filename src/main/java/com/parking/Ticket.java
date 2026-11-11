package com.parking;

import java.time.Instant;

public record Ticket(String id, Vehicle vehicle, ParkingSlot slot, Instant entryTime) { }
