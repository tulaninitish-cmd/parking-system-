package com.parking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core service. Public methods are synchronized so two vehicles can never be
 * given the same slot, even when many threads call park() at once.
 */
public class ParkingLot {
    private final List<ParkingFloor> floors;
    private final PricingStrategy pricing;
    private final Clock clock;
    private final Map<String, Ticket> activeTickets = new HashMap<>();
    private final Set<String> parkedPlates = new HashSet<>();
    private int ticketCounter = 0;

    public ParkingLot(List<ParkingFloor> floors, PricingStrategy pricing, Clock clock) {
        this.floors = List.copyOf(floors);
        this.pricing = pricing;
        this.clock = clock;
    }

    public ParkingLot(List<ParkingFloor> floors, PricingStrategy pricing) {
        this(floors, pricing, Clock.systemUTC());
    }

    public synchronized Ticket park(Vehicle vehicle) {
        if (parkedPlates.contains(vehicle.getPlate())) {
            throw new DuplicateVehicleException(vehicle.getPlate() + " is already parked");
        }
        ParkingSlot slot = floors.stream()
                .flatMap(f -> f.getSlots().stream())
                .filter(s -> s.canFit(vehicle))
                // best fit: smallest suitable slot, then lowest floor
                .min(Comparator.comparing(ParkingSlot::getType).thenComparingInt(ParkingSlot::getFloor))
                .orElseThrow(() -> new ParkingFullException("No free slot for " + vehicle.getType()));
        slot.occupy(vehicle);
        parkedPlates.add(vehicle.getPlate());
        Ticket ticket = new Ticket("T-" + (++ticketCounter), vehicle, slot, clock.instant());
        activeTickets.put(ticket.id(), ticket);
        return ticket;
    }

    public synchronized Receipt exit(String ticketId) {
        Ticket ticket = activeTickets.remove(ticketId);
        if (ticket == null) {
            throw new InvalidTicketException("Unknown or already used ticket: " + ticketId);
        }
        Instant now = clock.instant();
        Duration parked = Duration.between(ticket.entryTime(), now);
        ticket.slot().release();
        parkedPlates.remove(ticket.vehicle().getPlate());
        long fee = pricing.calculate(ticket.vehicle().getType(), parked);
        return new Receipt(ticket.id(), ticket.vehicle().getPlate(), parked, fee);
    }

    public synchronized Map<SlotType, Long> availability() {
        Map<SlotType, Long> free = new EnumMap<>(SlotType.class);
        for (SlotType t : SlotType.values()) free.put(t, 0L);
        floors.forEach(f -> f.getSlots().stream().filter(ParkingSlot::isFree)
                .forEach(s -> free.merge(s.getType(), 1L, Long::sum)));
        return free;
    }
}
