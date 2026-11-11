package com.parking;

public class Bike extends Vehicle {
    public Bike(String plate) { super(plate); }

    @Override
    public VehicleType getType() { return VehicleType.BIKE; }
}
