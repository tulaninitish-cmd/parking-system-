package com.parking;

import java.time.Duration;

/** Strategy pattern: pricing rules can be swapped without touching ParkingLot. */
public interface PricingStrategy {
    long calculate(VehicleType type, Duration parked);
}
