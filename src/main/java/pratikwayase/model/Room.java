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
    private volatile RoomStatus status; 
    private final double bookingPrice;
    private final boolean isSmoking;
    public final List<RoomBooking> bookings = Collections.synchronizedList(new ArrayList<>());
    private final ReentrantLock lock = new ReentrantLock(); 

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


    public boolean isRoomAvailable(Date checkInDate, int durationInDays) {
        // Basic input validation
        if (checkInDate == null || durationInDays <= 0) {
            return false;
        }

        lock.lock(); // Acquire the lock for this room
        try {
       
            if (status != RoomStatus.AVAILABLE) {
                return false;
            }

   
            Calendar requestedEnd = Calendar.getInstance();
            requestedEnd.setTime(checkInDate);
            requestedEnd.add(Calendar.DAY_OF_MONTH, durationInDays);

            synchronized (bookings) {
                for (RoomBooking booking : bookings) {
                   
                    if (booking.getStatus() == BookingStatus.CONFIRMED ||
                            booking.getStatus() == BookingStatus.CHECKED_IN) {

                        // Calculate the end date for the existing booking
                        Calendar existingBookingEnd = Calendar.getInstance();
                        existingBookingEnd.setTime(booking.getStartDate());
                        existingBookingEnd.add(Calendar.DAY_OF_MONTH, booking.getDurationInDays());

                      
                        if (checkInDate.before(existingBookingEnd.getTime()) &&
                                booking.getStartDate().before(requestedEnd.getTime())) {
                            return false; 
                        }
                    }
                }
            }
            return true; 
        } finally {
            lock.unlock(); 
        }
    }


    public void checkIn() {
        lock.lock();
        try {
            this.status = RoomStatus.OCCUPIED;
            System.out.println("Room " + roomNumber + " checked in.");
        } finally {
            lock.unlock();
        }
    }

    public void checkOut() {
        lock.lock();
        try {
            this.status = RoomStatus.AVAILABLE;
            System.out.println("Room " + roomNumber + " checked out.");
        } finally {
            lock.unlock();
        }
    }


    public void addBooking(RoomBooking booking) {
        lock.lock();
        try {
            this.bookings.add(booking);
        } finally {
            lock.unlock();
        }
    }


    public void removeBooking(RoomBooking booking) {
        lock.lock();
        try {
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
