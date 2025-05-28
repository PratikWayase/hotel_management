package pratikwayase.command;

import pratikwayase.model.RoomBooking;
import pratikwayase.enums.BookingStatus;



import pratikwayase.model.Hotel;
import pratikwayase.model.RoomBooking;
import pratikwayase.enums.BookingStatus;
import pratikwayase.exceptions.InvalidBookingException;

public class CancelBookingCommand implements Command {
    private final Hotel hotel;
    private final String reservationNumber;

    public CancelBookingCommand(Hotel hotel, String reservationNumber) {
        this.hotel = hotel;
        this.reservationNumber = reservationNumber;
    }

    @Override
    public void execute() throws InvalidBookingException {
        System.out.println("Attempting to cancel booking for reservation: " + reservationNumber);
        RoomBooking booking = hotel.findBookingByReservationNumber(reservationNumber);
        if (booking == null) {
            throw new InvalidBookingException("Booking with reservation number " + reservationNumber + " not found.");
        }
        hotel.cancelBooking(booking); // Hotel handles the cancellation logic and updates
        System.out.println("Booking " + reservationNumber + " cancelled successfully.");
    }
}