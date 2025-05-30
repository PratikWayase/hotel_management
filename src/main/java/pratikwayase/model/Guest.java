package pratikwayase.model;

import pratikwayase.Observer.Observer;
import java.util.*;

import pratikwayase.enums.*;
import pratikwayase.Observer.*;
import pratikwayase.events.*;
import java.util.*;


public class Guest extends User implements Observer<BookingConfirmationEvent> {
    
    private final List<RoomBooking> bookings = Collections.synchronizedList(new ArrayList<>());

    public Guest(String id, String name, String email, String phone) {
        super(id, name, email, phone, AccountType.GUEST);
    }

    public List<RoomBooking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public void addBooking(RoomBooking booking) {
        this.bookings.add(booking);
    }

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
