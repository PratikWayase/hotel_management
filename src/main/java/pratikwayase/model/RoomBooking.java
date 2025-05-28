package pratikwayase.model;


import pratikwayase.enums.BookingStatus;
import  pratikwayase.exceptions.InvalidBookingException;
import java.util.*;



public class RoomBooking {
    private final String reservationNumber;
    // Defensive copies for Date objects to ensure immutability
    private final Date startDate;
    private final int durationInDays;
    private volatile BookingStatus status; // volatile for visibility across threads
    private final Room room;
    private final Guest guest; // Link booking to a Guest
    private final double totalPrice; // Calculated based on room price and duration

    public RoomBooking(String reservationNumber, Room room, Guest guest, Date startDate, int durationInDays) throws InvalidBookingException {
        // Added input validation
        if (reservationNumber == null || reservationNumber.trim().isEmpty()) {
            throw new InvalidBookingException("Reservation number cannot be empty.");
        }
        if (room == null) {
            throw new InvalidBookingException("Room cannot be null for a booking.");
        }
        if (guest == null) {
            throw new InvalidBookingException("Guest cannot be null for a booking.");
        }
        // Allowing today's date for booking, but not past dates
        if (startDate == null || startDate.before(new Date(System.currentTimeMillis() - 86400000))) { // -86400000 ms = 1 day to allow current day
            throw new InvalidBookingException("Start date cannot be in the past.");
        }
        if (durationInDays <= 0) {
            throw new InvalidBookingException("Duration must be positive.");
        }

        this.reservationNumber = reservationNumber;
        this.room = room;
        this.guest = guest;
        this.startDate = new Date(startDate.getTime()); // Defensive copy
        this.durationInDays = durationInDays;
        this.status = BookingStatus.PENDING;
        this.totalPrice = room.getBookingPrice() * durationInDays; // Calculate total price
    }

    public String getReservationNumber() { return reservationNumber; }
    public Room getRoom() { return room; }
    public Guest getGuest() { return guest; }
    public int getDurationInDays() { return durationInDays; }
    public BookingStatus getStatus() { return status; }
    public double getTotalPrice() { return totalPrice; }

    // Return a defensive copy of startDate to prevent external modification
    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    // This setter is volatile, but for complex state transitions,
    // a synchronized block or lock might be needed if multiple threads
    // could try to change status simultaneously in a conflicting way.
    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RoomBooking{" +
                "reservationNumber='" + reservationNumber + '\'' +
                ", room=" + (room != null ? room.getRoomNumber() : "N/A") +
                ", guest=" + (guest != null ? guest.getName() : "N/A") +
                ", startDate=" + startDate +
                ", durationInDays=" + durationInDays +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                '}';
    }
}