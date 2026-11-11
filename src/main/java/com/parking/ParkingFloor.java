package com.parking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParkingFloor {
    private final int number;
    private final List<ParkingSlot> slots = new ArrayList<>();

    public ParkingFloor(int number, int small, int medium, int large) {
        this.number = number;
        add(SlotType.SMALL, small);
        add(SlotType.MEDIUM, medium);
        add(SlotType.LARGE, large);
    }

    private void add(SlotType type, int count) {
        for (int i = 0; i < count; i++) {
            slots.add(new ParkingSlot("F" + number + "-" + type.name().charAt(0) + (i + 1), number, type));
        }
    }

    public int getNumber() { return number; }
    public List<ParkingSlot> getSlots() { return Collections.unmodifiableList(slots); }
}
