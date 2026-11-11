package com.parking;

public class ParkingSlot {
    private final String id;
    private final int floor;
    private final SlotType type;
    private Vehicle occupant;

    public ParkingSlot(String id, int floor, SlotType type) {
        this.id = id;
        this.floor = floor;
        this.type = type;
    }

    public String getId() { return id; }
    public int getFloor() { return floor; }
    public SlotType getType() { return type; }
    public boolean isFree() { return occupant == null; }

    public boolean canFit(Vehicle vehicle) { return isFree() && vehicle.getType().fitsIn(type); }

    void occupy(Vehicle vehicle) { this.occupant = vehicle; }
    void release() { this.occupant = null; }

    @Override
    public String toString() { return id + " (" + type + ")"; }
}
