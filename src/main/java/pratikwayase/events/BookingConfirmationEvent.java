package pratikwayase.events;

public class BookingConfirmationEvent {
    private final String message;
    private final String reservationNumber;
    private final String roomNumber;

    public BookingConfirmationEvent(String message, String reservationNumber, String roomNumber) {
        this.message = message;
        this.reservationNumber = reservationNumber;
        this.roomNumber = roomNumber;
    }

    public String getMessage() {
        return message;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }
}