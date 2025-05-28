package pratikwayase.model;


import pratikwayase.enums.AccountType;
import pratikwayase.enums.BookingStatus;

public class Receptionist extends User {
    public Receptionist(String id, String name, String email, String phone) {
        super(id, name, email, phone, AccountType.RECEPTIONIST);
    }

    @Override
    public void displayInfo() {
        System.out.println("Receptionist Name: " + getName() + ", Email: " + getEmail());
    }

    /**
     * Handles checking in a guest for a specific booking.
     * Updates the room status and booking status.
     * @param booking The RoomBooking to check in.
     */
    public void checkInGuest(RoomBooking booking) {
        if (booking != null && booking.getRoom() != null) {
            booking.getRoom().checkIn(); // Room's checkIn method handles its own lock
            booking.setStatus(BookingStatus.CHECKED_IN);
            System.out.println("Guest checked in for booking " + booking.getReservationNumber());
        } else {
            System.out.println("Error: Cannot check in. Booking or associated room is null.");
        }
    }

    /**
     * Handles checking out a guest for a specific booking.
     * Updates the room status and booking status.
     * @param booking The RoomBooking to check out.
     */
    public void checkOutGuest(RoomBooking booking) {
        if (booking != null && booking.getRoom() != null) {
            booking.getRoom().checkOut(); // Room's checkOut method handles its own lock
            booking.setStatus(BookingStatus.CHECKED_OUT);
            System.out.println("Guest checked out for booking " + booking.getReservationNumber());
        } else {
            System.out.println("Error: Cannot check out. Booking or associated room is null.");
        }
    }
}