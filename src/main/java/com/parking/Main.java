package com.parking;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot(
                List.of(new ParkingFloor(1, 2, 3, 1), new ParkingFloor(2, 2, 3, 1)),
                new HourlyPricing());
        Scanner in = new Scanner(System.in);
        System.out.println("=== Parking System ===");
        while (true) {
            System.out.print("\n1) Park  2) Exit  3) Availability  0) Quit > ");
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Type (BIKE/CAR/TRUCK): ");
                        VehicleType type = VehicleType.valueOf(in.nextLine().trim().toUpperCase());
                        System.out.print("Number plate: ");
                        Ticket t = lot.park(VehicleFactory.create(type, in.nextLine()));
                        System.out.println("Ticket " + t.id() + " -> slot " + t.slot());
                    }
                    case "2" -> {
                        System.out.print("Ticket id: ");
                        Receipt r = lot.exit(in.nextLine().trim());
                        System.out.println("Plate " + r.plate() + ", parked " + r.duration().toMinutes()
                                + " min, fee Rs " + r.fee());
                    }
                    case "3" -> System.out.println("Free slots: " + lot.availability());
                    case "0" -> { return; }
                    default -> System.out.println("Choose 0-3.");
                }
            } catch (IllegalArgumentException | ParkingFullException
                     | InvalidTicketException | DuplicateVehicleException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
