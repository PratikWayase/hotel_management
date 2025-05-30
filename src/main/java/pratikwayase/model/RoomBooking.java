package pratikwayase.model;


import pratikwayase.enums.BookingStatus;
import  pratikwayase.exceptions.InvalidBookingException;
import java.util.*;



public class RoomBooking {
    private final String reservationNumber;
    private final Date startDate;
    private final int durationInDays;
    private volatile BookingStatus status; 
    private final Room room;
    private final Guest guest; 
    private final double totalPrice; 
    public RoomBooking(String reservationNumber, Room room, Guest guest, Date startDate, int durationInDays) throws InvalidBookingException {
        if (reservationNumber == null || reservationNumber.trim().isEmpty()) {
            throw new InvalidBookingException("Reservation number cannot be empty.");
        }
        if (room == null) {
            throw new InvalidBookingException("Room cannot be null for a booking.");
        }
        if (guest == null) {
            throw new InvalidBookingException("Guest cannot be null for a booking.");
        }
       
        if (startDate == null || startDate.before(new Date(System.currentTimeMillis() - 86400000))) { // -86400000 ms = 1 day to allow current day
            throw new InvalidBookingException("Start date cannot be in the past.");
        }
        if (durationInDays <= 0) {
            throw new InvalidBookingException("Duration must be positive.");
        }

        this.reservationNumber = reservationNumber;
        this.room = room;
        this.guest = guest;
        this.startDate = new Date(startDate.getTime()); 
        this.durationInDays = durationInDays;
        this.status = BookingStatus.PENDING;
        this.totalPrice = room.getBookingPrice() * durationInDays; 
    }

    public String getReservationNumber() { return reservationNumber; }
    public Room getRoom() { return room; }
    public Guest getGuest() { return guest; }
    public int getDurationInDays() { return durationInDays; }
    public BookingStatus getStatus() { return status; }
    public double getTotalPrice() { return totalPrice; }

    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

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
