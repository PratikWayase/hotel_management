package pratikwayase.model;

import java.util.*;
import java.util.concurrent.*;
import  pratikwayase.exceptions.InvalidBookingException;

import  pratikwayase.enums.RoomStyle;
import  pratikwayase.enums.BookingStatus;
import  pratikwayase.exceptions.RoomNotAvailableException;
import  pratikwayase.exceptions.InvalidBookingException;
import  pratikwayase.strategy.SearchStrategy;
import  pratikwayase.Observer.SystemNotifier;
import  pratikwayase.events.BookingConfirmationEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// Central Hotel class to manage operations and orchestrate interactions
public class Hotel {
    private final String name;
    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();
    public final List<RoomBooking> bookings = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();
    // This notifier can remain for *general* system-wide notifications,
    // but specific booking confirmations should go directly to the guest.
    // private final SystemNotifier<BookingConfirmationEvent> bookingNotifier = new SystemNotifier<>();

    public Hotel(String name) {
        this.name = name;
    }


    /**
     * Adds a room to the hotel's collection. Thread-safe due to ConcurrentHashMap.
     * @param room The Room object to add.
     */
    public void addRoom(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    /**
     * Adds a user to the hotel's collection.
     * We will remove the direct Observer registration here as it's too broad for booking notifications.
     * @param user The User object to add.
     */
    public void addUser(User user) {
        users.put(user.getId(), user);
        // Removed: if (user instanceof Guest) { bookingNotifier.addObserver((Guest) user); }
        // Booking confirmations will now be sent directly to the relevant Guest object.
    }

    /**
     * Finds a user by their ID. Thread-safe.
     * @param userId The ID of the user to find.
     * @return The User object if found, null otherwise.
     */
    public User findUserById(String userId) {
        return users.get(userId);
    }

    /**
     * Finds a room by its room number. Thread-safe.
     * @param roomNumber The number of the room to find.
     * @return The Room object if found, null otherwise.
     */
    public Room findRoomByNumber(String roomNumber) {
        return rooms.get(roomNumber);
    }

    /**
     * Finds a booking by its reservation number. Thread-safe.
     * @param reservationNumber The reservation number of the booking to find.
     * @return The RoomBooking object if found, null otherwise.
     */
    public RoomBooking findBookingByReservationNumber(String reservationNumber) {
        synchronized (bookings) {
            return bookings.stream()
                    .filter(b -> b.getReservationNumber().equals(reservationNumber))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Searches for available rooms using a given search strategy.
     * @param strategy The SearchStrategy to use (e.g., by style, by availability).
     * @param style The RoomStyle to search for (can be null if not filtering by style).
     * @param startDate The desired check-in date.
     * @param duration The desired duration in days.
     * @return A list of rooms matching the criteria.
     */
    public List<Room> searchRooms(SearchStrategy strategy, RoomStyle style, Date startDate, int duration) {
        return strategy.searchRooms(new ArrayList<>(rooms.values()), style, startDate, duration);
    }

    /**
     * Creates a new room booking. This method ensures thread-safe booking creation
     * by relying on the Room's internal locking for availability checks and updates.
     * @param reservationNumber Unique identifier for the booking.
     * @param roomNumber The number of the room to book.
     * @param guestId The ID of the guest making the booking.
     * @param startDate The check-in date.
     * @param durationInDays The duration of the stay.
     * @return The newly created RoomBooking object.
     * @throws RoomNotAvailableException If the room is not available for the requested dates.
     * @throws InvalidBookingException If input data is invalid or room/guest not found.
     */
    public RoomBooking createBooking(String reservationNumber, String roomNumber, String guestId, Date startDate, int durationInDays)
            throws RoomNotAvailableException, InvalidBookingException {

        // Input validation (basic checks before acquiring locks)
        if (startDate == null || startDate.before(new Date(System.currentTimeMillis() - 86400000))) {
            throw new InvalidBookingException("Start date cannot be in the past.");
        }
        if (durationInDays <= 0) {
            throw new InvalidBookingException("Duration must be positive.");
        }
        if (reservationNumber == null || reservationNumber.trim().isEmpty()) {
            throw new InvalidBookingException("Reservation number cannot be empty.");
        }

        Room room = rooms.get(roomNumber);
        if (room == null) {
            throw new InvalidBookingException("Room " + roomNumber + " not found.");
        }

        User user = users.get(guestId);
        if (!(user instanceof Guest)) {
            throw new InvalidBookingException("User with ID " + guestId + " is not a valid guest.");
        }
        Guest guest = (Guest) user;

        if (!room.isRoomAvailable(startDate, durationInDays)) {
            throw new RoomNotAvailableException("Room " + roomNumber + " is not available for the requested dates.");
        }

        RoomBooking booking = new RoomBooking(reservationNumber, room, guest, startDate, durationInDays);

        synchronized (bookings) {
            bookings.add(booking);
        }
        room.addBooking(booking);
        guest.addBooking(booking); // The guest object adds the booking to its own list

        System.out.println("Booking created: " + booking.getReservationNumber() + " for " + room.getRoomNumber());
        return booking;
    }


    public void confirmBooking(RoomBooking booking) {
        if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            System.out.println("Booking " + booking.getReservationNumber() + " confirmed.");

            // Notify ONLY THE GUEST WHO MADE THE BOOKING
            booking.getGuest().update(new BookingConfirmationEvent(
                    "Your booking " + booking.getReservationNumber() + " has been confirmed!",
                    booking.getReservationNumber(),
                    booking.getRoom().getRoomNumber()
            ));
        } else {
            System.out.println("Could not confirm booking: " + (booking != null ? booking.getReservationNumber() : "null") + ". Status: " + (booking != null ? booking.getStatus() : "N/A"));
        }
    }

    /**
     * Cancels a booking and notifies only the relevant guest.
     * @param booking The RoomBooking to cancel.
     */
    public void cancelBooking(RoomBooking booking) {
        if (booking != null && (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED)) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getRoom().removeBooking(booking);
            booking.getGuest().removeBooking(booking);
            synchronized (bookings) {
                bookings.remove(booking);
            }
            System.out.println("Booking " + booking.getReservationNumber() + " cancelled.");

            // Notify ONLY THE GUEST WHOSE BOOKING WAS CANCELLED
            booking.getGuest().update(new BookingConfirmationEvent(
                    "Your booking " + booking.getReservationNumber() + " has been cancelled.",
                    booking.getReservationNumber(),
                    booking.getRoom().getRoomNumber()
            ));
        } else {
            System.out.println("Could not cancel booking: " + (booking != null ? booking.getReservationNumber() : "null") + ". Status: " + (booking != null ? booking.getStatus() : "N/A"));
        }
    }

    public String getName() {
        return name;
    }

}