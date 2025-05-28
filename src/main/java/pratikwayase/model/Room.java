package pratikwayase.model;

import pratikwayase.enums.RoomStatus;
import  pratikwayase.enums.RoomStyle;
import  pratikwayase.exceptions.InvalidBookingException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import  pratikwayase.enums.BookingStatus;


public class Room {
    private final String roomNumber;
    private final RoomStyle style;
    private volatile RoomStatus status; // volatile ensures visibility of status changes across threads
    private final double bookingPrice;
    private final boolean isSmoking;
    // Using Collections.synchronizedList for thread-safe access to the bookings list
    public final List<RoomBooking> bookings = Collections.synchronizedList(new ArrayList<>());
    private final ReentrantLock lock = new ReentrantLock(); // Fine-grained lock for this specific room

    public Room(String roomNumber, RoomStyle style, double bookingPrice, boolean isSmoking)
            throws InvalidBookingException {
        // Added input validation
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new InvalidBookingException("Room number cannot be empty.");
        }
        if (bookingPrice <= 0) {
            throw new InvalidBookingException("Booking price must be positive.");
        }

        this.roomNumber = roomNumber;
        this.style = style;
        this.bookingPrice = bookingPrice;
        this.isSmoking = isSmoking;
        this.status = RoomStatus.AVAILABLE;
    }

    public String getRoomNumber() { return roomNumber; }
    public RoomStyle getStyle() { return style; }
    public RoomStatus getStatus() { return status; }
    public double getBookingPrice() { return bookingPrice; }

    /**
     * Checks if the room is available for the given dates, considering existing bookings.
     * This method is thread-safe using the room's ReentrantLock.
     * @param checkInDate The requested check-in date.
     * @param durationInDays The requested duration in days.
     * @return true if the room is available, false otherwise.
     */
    public boolean isRoomAvailable(Date checkInDate, int durationInDays) {
        // Basic input validation
        if (checkInDate == null || durationInDays <= 0) {
            return false;
        }

        lock.lock(); // Acquire the lock for this room
        try {
            // First, check the general status of the room
            if (status != RoomStatus.AVAILABLE) {
                return false;
            }

            // Calculate the end date for the requested booking
            Calendar requestedEnd = Calendar.getInstance();
            requestedEnd.setTime(checkInDate);
            requestedEnd.add(Calendar.DAY_OF_MONTH, durationInDays);

            // Iterate through existing bookings to check for overlaps
            // The 'bookings' list is already synchronized, but we're inside the room's lock,
            // which provides an extra layer of consistency for this critical check.
            synchronized (bookings) { // Synchronize on the list itself for iteration safety
                for (RoomBooking booking : bookings) {
                    // Only consider confirmed or checked-in bookings for availability checks
                    if (booking.getStatus() == BookingStatus.CONFIRMED ||
                            booking.getStatus() == BookingStatus.CHECKED_IN) {

                        // Calculate the end date for the existing booking
                        Calendar existingBookingEnd = Calendar.getInstance();
                        existingBookingEnd.setTime(booking.getStartDate());
                        existingBookingEnd.add(Calendar.DAY_OF_MONTH, booking.getDurationInDays());

                        // Check for overlap: [start1, end1) overlaps with [start2, end2) if start1 < end2 AND start2 < end1
                        // Using defensive copies of dates from RoomBooking to avoid external modification issues
                        if (checkInDate.before(existingBookingEnd.getTime()) &&
                                booking.getStartDate().before(requestedEnd.getTime())) {
                            return false; // Overlap detected, room is not available
                        }
                    }
                }
            }
            return true; // No overlaps found, room is available
        } finally {
            lock.unlock(); // Always release the lock
        }
    }

    /**
     * Changes room status to OCCUPIED. Thread-safe.
     */
    public void checkIn() {
        lock.lock();
        try {
            this.status = RoomStatus.OCCUPIED;
            System.out.println("Room " + roomNumber + " checked in.");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Changes room status to AVAILABLE. Thread-safe.
     */
    public void checkOut() {
        lock.lock();
        try {
            this.status = RoomStatus.AVAILABLE;
            System.out.println("Room " + roomNumber + " checked out.");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds a booking to this room's internal list. Thread-safe.
     * @param booking The RoomBooking to add.
     */
    public void addBooking(RoomBooking booking) {
        lock.lock();
        try {
            // The list itself is synchronized, but the room lock ensures consistency
            // if other operations (like status checks) are happening concurrently.
            this.bookings.add(booking);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a booking from this room's internal list. Thread-safe.
     * @param booking The RoomBooking to remove.
     */
    public void removeBooking(RoomBooking booking) {
        lock.lock();
        try {
            // The list itself is synchronized, but the room lock ensures consistency.
            this.bookings.remove(booking);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", style=" + style +
                ", status=" + status +
                ", bookingPrice=" + bookingPrice +
                '}';
    }
}