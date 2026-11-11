package com.parking;

/** Vehicle categories, each with the smallest slot it can use. */
public enum VehicleType {
    BIKE(SlotType.SMALL), CAR(SlotType.MEDIUM), TRUCK(SlotType.LARGE);

    private final SlotType minimumSlot;

    VehicleType(SlotType minimumSlot) { this.minimumSlot = minimumSlot; }

    public boolean fitsIn(SlotType slot) { return slot.ordinal() >= minimumSlot.ordinal(); }
}
