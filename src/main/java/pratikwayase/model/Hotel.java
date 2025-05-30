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

public class Hotel {
    private final String name;
    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();
    public final List<RoomBooking> bookings = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();
  

    public Hotel(String name) {
        this.name = name;
    }


    public void addRoom(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    
    public User findUserById(String userId) {
        return users.get(userId);
    }


    public Room findRoomByNumber(String roomNumber) {
        return rooms.get(roomNumber);
    }


    public RoomBooking findBookingByReservationNumber(String reservationNumber) {
        synchronized (bookings) {
            return bookings.stream()
                    .filter(b -> b.getReservationNumber().equals(reservationNumber))
                    .findFirst()
                    .orElse(null);
        }
    }


    public List<Room> searchRooms(SearchStrategy strategy, RoomStyle style, Date startDate, int duration) {
        return strategy.searchRooms(new ArrayList<>(rooms.values()), style, startDate, duration);
    }


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
