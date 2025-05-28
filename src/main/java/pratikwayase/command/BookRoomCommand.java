package pratikwayase.command;

import pratikwayase.model.RoomBooking;
import pratikwayase.enums.BookingStatus;

import java.util.Date;



import pratikwayase.model.Hotel;
import pratikwayase.model.RoomBooking;
import pratikwayase.enums.BookingStatus;
import pratikwayase.exceptions.RoomNotAvailableException;
import pratikwayase.exceptions.InvalidBookingException;
import java.util.Date;

public class BookRoomCommand implements Command {
    private final Hotel hotel;
    private final String reservationNumber;
    private final String roomNumber;
    private final String guestId;
    private final Date startDate;
    private final int durationInDays;
    private RoomBooking createdBooking; // To hold the created booking after execution

    public BookRoomCommand(Hotel hotel, String reservationNumber, String roomNumber, String guestId, Date startDate, int durationInDays) {
        this.hotel = hotel;
        this.reservationNumber = reservationNumber;
        this.roomNumber = roomNumber;
        this.guestId = guestId;
        this.startDate = startDate;
        this.durationInDays = durationInDays;
    }

    @Override
    public void execute() throws RoomNotAvailableException, InvalidBookingException {
        System.out.println("Attempting to book room for reservation: " + reservationNumber);
        // The hotel.createBooking method handles the concurrency logic internally
        createdBooking = hotel.createBooking(reservationNumber, roomNumber, guestId, startDate, durationInDays);
        hotel.confirmBooking(createdBooking); // Confirm the booking automatically after creation
        System.out.println("Booking for reservation " + reservationNumber + " confirmed.");
    }

    public RoomBooking getCreatedBooking() {
        return createdBooking;
    }
}