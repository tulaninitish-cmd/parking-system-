package com.parking;

public abstract class Vehicle {
    private final String plate;

    protected Vehicle(String plate) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("Number plate must not be empty");
        }
        this.plate = plate.trim().toUpperCase();
    }

    public String getPlate() { return plate; }

    public abstract VehicleType getType();

    @Override
    public String toString() { return getType() + " " + plate; }
}
