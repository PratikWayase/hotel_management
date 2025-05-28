package pratikwayase.model;

import pratikwayase.Observer.Observer;
import java.util.*;

import pratikwayase.enums.*;
import pratikwayase.Observer.*;
import pratikwayase.events.*;
import java.util.*;


// Guest extends User and implements Observer to receive booking notifications
public class Guest extends User implements Observer<BookingConfirmationEvent> {
    // Using Collections.synchronizedList for thread-safe access to the bookings list
    private final List<RoomBooking> bookings = Collections.synchronizedList(new ArrayList<>());

    public Guest(String id, String name, String email, String phone) {
        super(id, name, email, phone, AccountType.GUEST);
    }

    // Returns an unmodifiable view of the bookings list to prevent external modification
    public List<RoomBooking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    // Adds a booking to the guest's list (thread-safe due to synchronizedList)
    public void addBooking(RoomBooking booking) {
        this.bookings.add(booking);
    }

    // Removes a booking from the guest's list (thread-safe due to synchronizedList)
    public void removeBooking(RoomBooking booking) {
        this.bookings.remove(booking);
    }

    @Override
    public void update(BookingConfirmationEvent event) { // Updated to receive BookingConfirmationEvent
        System.out.println("Notification for Guest: " + getName() + ": " + event.getMessage() +
                " (Booking Ref: " + event.getReservationNumber() +
                ", Room: " + event.getRoomNumber() + ")");
    }

    @Override
    public void displayInfo() {
        System.out.println("Guest Name: " + getName() + ", Email: " + getEmail() + ", Account Status: " + getAccountStatus());
    }
}
