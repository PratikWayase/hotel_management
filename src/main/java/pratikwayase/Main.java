package pratikwayase;

import pratikwayase.enums.RoomStyle;
import pratikwayase.factory.DeluxeRoomFactory;
import pratikwayase.model.Room;
import pratikwayase.model.RoomBooking;
import pratikwayase.command.*;
import pratikwayase.template.*;
import pratikwayase.Observer.*;
import pratikwayase.model.Guest;

import java.util.Date;

import pratikwayase.factory.FamilySuiteRoomFactory;

import pratikwayase.model.Hotel;

import pratikwayase.model.Receptionist;
import pratikwayase.command.*;
import pratikwayase.template.*;
import pratikwayase.exceptions.RoomNotAvailableException;
import pratikwayase.exceptions.InvalidBookingException;
import pratikwayase.strategy.RoomStyleSearchStrategy;
import pratikwayase.strategy.RoomAvailabilitySearchStrategy;
import pratikwayase.strategy.SearchStrategy;

import java.util.List;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
   
        Hotel grandHyatt = new Hotel("Grand Hyatt Mumbai");

        // Create Rooms using Factories
        DeluxeRoomFactory deluxeFactory = new DeluxeRoomFactory();
        FamilySuiteRoomFactory familySuiteFactory = new FamilySuiteRoomFactory();

        try {
            Room room101 = deluxeFactory.createRoom(RoomStyle.DELUXE, "101", 200.0, false);
            Room room102 = deluxeFactory.createRoom(RoomStyle.DELUXE, "102", 200.0, true);
            Room room201 = familySuiteFactory.createRoom(RoomStyle.FAMILY_SUITE, "201", 450.0, false);
            Room room202 = familySuiteFactory.createRoom(RoomStyle.FAMILY_SUITE, "202", 450.0, false);

            grandHyatt.addRoom(room101);
            grandHyatt.addRoom(room102);
            grandHyatt.addRoom(room201);
            grandHyatt.addRoom(room202);

            System.out.println("--- Initial Rooms Added ---");
            // Search for all available rooms for a duration of 1 day from now
            grandHyatt.searchRooms(new RoomAvailabilitySearchStrategy(), null, new Date(), 1)
                    .forEach(room -> System.out.println("Available: " + room));

            // Create Users
            Guest john = new Guest("G001", "John Doe", "john.doe@example.com", "123-456-7890");
            Guest jane = new Guest("G002", "Jane Smith", "jane.smith@example.com", "987-654-3210");
            Guest mark = new Guest("G003", "Mark Johnson", "mark.j@example.com", "111-222-3333"); // New guest for concurrency test
            Receptionist alice = new Receptionist("R001", "Alice Brown", "alice.b@hotel.com", "555-111-2222");

            grandHyatt.addUser(john);
            grandHyatt.addUser(jane);
            grandHyatt.addUser(mark);
            grandHyatt.addUser(alice);

            System.out.println("\n--- Users Created ---");
            john.displayInfo();
            alice.displayInfo();
            mark.displayInfo();

            // ---  Command Pattern ---
            System.out.println("\n--- Demonstrating Command Pattern (Booking & Cancellation) ---");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 7); // Booking for 7 days from now
            Date bookingDate = cal.getTime();

            
            System.out.println("\nAttempting to book Room 101 for John...");
            Command bookRoom101ForJohn = new BookRoomCommand(grandHyatt, "RES001", "101", "G001", bookingDate, 3);
            bookRoom101ForJohn.execute();
            System.out.println("John's bookings: " + john.getBookings())
            System.out.println("Room 101 internal bookings: " + grandHyatt.findRoomByNumber("101").bookings);
            System.out.println("\nAttempting to book Room 101 again for Jane (should fail)...");
            try {
                Command bookRoom101ForJane = new BookRoomCommand(grandHyatt, "RES002", "101", "G002", bookingDate, 2);
                bookRoom101ForJane.execute();
            } catch (RoomNotAvailableException | InvalidBookingException e) {
                System.err.println("Booking failed as expected: " + e.getMessage());
            }

            // Book Room 201 for Jane
            System.out.println("\nAttempting to book Room 201 for Jane...");
            Command bookRoom201ForJane = new BookRoomCommand(grandHyatt, "RES003", "201", "G002", bookingDate, 5);
            bookRoom201ForJane.execute();
            System.out.println("Jane's bookings: " + jane.getBookings());


            // Cancel Booking RES001
            System.out.println("\n--- Cancelling Booking RES001 ---");
            Command cancelBooking1 = new CancelBookingCommand(grandHyatt, "RES001");
            cancelBooking1.execute();
            System.out.println("John's bookings after cancellation: " + john.getBookings());
            // Directly accessing room's internal bookings list for verification (for demo purposes)
            System.out.println("Room 101 internal bookings after cancellation: " + grandHyatt.findRoomByNumber("101").bookings);


            // --- Template Method Pattern ---
            System.out.println("\n--- Demonstrating Template Method Pattern (Room Services) ---");
            RoomServiceTemplate housekeeping = new HousekeepingService();
            housekeeping.executeService(room102); 

            RoomServiceTemplate foodService = new FoodService();
            foodService.executeService(room201); 

            // --- Demonstrate Strategy Pattern for Searching Rooms ---
            System.out.println("\n--- Demonstrating Strategy Pattern (Searching Rooms) ---");

            // Search for available Deluxe rooms for specific dates
            System.out.println("Searching for available DELUXE rooms for " + bookingDate.toGMTString() + " for 3 days:");
            SearchStrategy styleSearch = new RoomStyleSearchStrategy();
            List<Room> availableDeluxeRooms = grandHyatt.searchRooms(styleSearch, RoomStyle.DELUXE, bookingDate, 3);
            if (availableDeluxeRooms.isEmpty()) {
                System.out.println("No Deluxe rooms available for these dates.");
            } else {
                availableDeluxeRooms.forEach(room -> System.out.println("Found: " + room));
            }

            // Search for all available rooms regardless of style for specific dates
            System.out.println("\nSearching for ALL available rooms for " + bookingDate.toGMTString() + " for 3 days:");
            SearchStrategy availabilitySearch = new RoomAvailabilitySearchStrategy();
            List<Room> allAvailableRooms = grandHyatt.searchRooms(availabilitySearch, null, bookingDate, 3); 
            if (allAvailableRooms.isEmpty()) {
                System.out.println("No rooms available for these dates.");
            } else {
                allAvailableRooms.forEach(room -> System.out.println("Found: " + room));
            }

            // ---  Receptionist Actions ---
            System.out.println("\n--- Demonstrating Receptionist Actions ---");
            RoomBooking janeBooking = grandHyatt.findBookingByReservationNumber("RES003");
            if (janeBooking != null) {
                alice.checkInGuest(janeBooking);
                System.out.println("Room 201 status after check-in: " + room201.getStatus());
            }

            // Simulate some time passing, then check out
            System.out.println("\nSimulating check-out for Jane...");
            if (janeBooking != null) {
                alice.checkOutGuest(janeBooking);
                System.out.println("Room 201 status after check-out: " + room201.getStatus());
            }
