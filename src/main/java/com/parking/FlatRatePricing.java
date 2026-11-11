package com.parking;

import java.time.Duration;

/** One fixed fee per visit, whatever the vehicle or duration. */
public class FlatRatePricing implements PricingStrategy {
    private final long fee;

    public FlatRatePricing(long fee) { this.fee = fee; }

    @Override
    public long calculate(VehicleType type, Duration parked) { return fee; }
}
