package com.parking;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

/** Charges per started hour, with a one-hour minimum. */
public class HourlyPricing implements PricingStrategy {
    private final Map<VehicleType, Long> ratePerHour = new EnumMap<>(VehicleType.class);

    public HourlyPricing() { this(10, 20, 40); }

    public HourlyPricing(long bike, long car, long truck) {
        ratePerHour.put(VehicleType.BIKE, bike);
        ratePerHour.put(VehicleType.CAR, car);
        ratePerHour.put(VehicleType.TRUCK, truck);
    }

    @Override
    public long calculate(VehicleType type, Duration parked) {
        long hours = Math.max(1, (parked.getSeconds() + 3599) / 3600);
        return hours * ratePerHour.get(type);
    }
}
