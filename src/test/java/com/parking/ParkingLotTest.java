package com.parking;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkingLotTest {

    /** Controllable clock so fee tests do not depend on real time. */
    static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");
        void advance(Duration d) { now = now.plus(d); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    TestClock clock;
    ParkingLot lot;

    @BeforeEach
    void setUp() {
        clock = new TestClock();
        lot = new ParkingLot(List.of(new ParkingFloor(1, 1, 1, 1)), new HourlyPricing(), clock);
    }

    @Test
    void bikeGetsSmallestSlot() {
        Ticket t = lot.park(new Bike("PB01"));
        assertEquals(SlotType.SMALL, t.slot().getType());
    }

    @Test
    void carNeverUsesSmallSlot() {
        Ticket t = lot.park(new Car("PB02"));
        assertEquals(SlotType.MEDIUM, t.slot().getType());
    }

    @Test
    void truckOnlyFitsLargeSlot() {
        assertEquals(SlotType.LARGE, lot.park(new Truck("PB03")).slot().getType());
        assertThrows(ParkingFullException.class, () -> lot.park(new Truck("PB04")));
    }

    @Test
    void bikeOverflowsIntoBiggerSlotsWhenSmallIsFull() {
        lot.park(new Bike("A1"));
        assertEquals(SlotType.MEDIUM, lot.park(new Bike("A2")).slot().getType());
    }

    @Test
    void fullLotThrows() {
        lot.park(new Car("C1"));
        lot.park(new Truck("T1"));
        assertThrows(ParkingFullException.class, () -> lot.park(new Car("C2")));
    }

    @Test
    void exitFreesTheSlot() {
        Ticket t = lot.park(new Truck("T1"));
        lot.exit(t.id());
        assertEquals(1L, lot.availability().get(SlotType.LARGE));
        assertDoesNotThrow(() -> lot.park(new Truck("T2")));
    }

    @Test
    void unknownTicketRejected() {
        assertThrows(InvalidTicketException.class, () -> lot.exit("T-999"));
    }

    @Test
    void ticketCannotBeUsedTwice() {
        Ticket t = lot.park(new Car("C1"));
        lot.exit(t.id());
        assertThrows(InvalidTicketException.class, () -> lot.exit(t.id()));
    }

    @Test
    void sameVehicleCannotParkTwice() {
        lot.park(new Car("C1"));
        assertThrows(DuplicateVehicleException.class, () -> lot.park(new Car(" c1 ")));
    }

    @Test
    void blankPlateRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Car("  "));
    }

    @Test
    void factoryBuildsCorrectSubclass() {
        assertInstanceOf(Truck.class, VehicleFactory.create(VehicleType.TRUCK, "X1"));
        assertEquals(VehicleType.BIKE, VehicleFactory.create(VehicleType.BIKE, "X2").getType());
    }

    @Test
    void hourlyFeeRoundsUpPartialHours() {
        Ticket t = lot.park(new Bike("B1"));
        clock.advance(Duration.ofMinutes(61));
        assertEquals(20, lot.exit(t.id()).fee());
    }

    @Test
    void hourlyFeeHasOneHourMinimum() {
        Ticket t = lot.park(new Car("C1"));
        assertEquals(20, lot.exit(t.id()).fee());
    }

    @Test
    void truckThreeHours() {
        Ticket t = lot.park(new Truck("T1"));
        clock.advance(Duration.ofHours(3));
        assertEquals(120, lot.exit(t.id()).fee());
    }

    @Test
    void flatPricingStrategyCanBeSwappedIn() {
        ParkingLot flat = new ParkingLot(List.of(new ParkingFloor(1, 0, 1, 0)), new FlatRatePricing(50), clock);
        Ticket t = flat.park(new Car("C1"));
        clock.advance(Duration.ofHours(5));
        assertEquals(50, flat.exit(t.id()).fee());
    }

    @Test
    void neverOverbooksUnderConcurrentLoad() throws Exception {
        ParkingLot busy = new ParkingLot(List.of(new ParkingFloor(1, 0, 10, 0)), new HourlyPricing(), clock);
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger parked = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            final int n = i;
            pool.submit(() -> {
                try {
                    start.await();
                    busy.park(new Car("CAR" + n));
                    parked.incrementAndGet();
                } catch (ParkingFullException e) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(10, parked.get());
        assertEquals(40, rejected.get());
    }
}
