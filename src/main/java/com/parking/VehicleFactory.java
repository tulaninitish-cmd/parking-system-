package com.parking;

/** Factory pattern: creates the right Vehicle subclass from a type. */
public final class VehicleFactory {
    private VehicleFactory() { }

    public static Vehicle create(VehicleType type, String plate) {
        return switch (type) {
            case BIKE -> new Bike(plate);
            case CAR -> new Car(plate);
            case TRUCK -> new Truck(plate);
        };
    }
}
