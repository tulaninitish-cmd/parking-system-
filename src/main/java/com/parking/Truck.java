package com.parking;

public class Truck extends Vehicle {
    public Truck(String plate) { super(plate); }

    @Override
    public VehicleType getType() { return VehicleType.TRUCK; }
}
