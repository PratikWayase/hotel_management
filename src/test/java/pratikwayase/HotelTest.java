package pratikwayase;

import pratikwayase.enums.BookingStatus;
import pratikwayase.enums.RoomStyle;
import pratikwayase.enums.RoomStatus;
import pratikwayase.exceptions.InvalidBookingException;
import pratikwayase.exceptions.RoomNotAvailableException;
import pratikwayase.factory.DeluxeRoomFactory;
import pratikwayase.factory.RoomFactory;
import pratikwayase.model.*;
import pratikwayase.command.*;
import pratikwayase.strategy.RoomAvailabilitySearchStrategy;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hotel Management System Tests")
class HotelTest {

    // Constants for test data
    private static final String HOTEL_NAME = "Grand Plaza";
    private static final String DELUXE_ROOM_NUMBER = "101";
    private static final String BUSINESS_ROOM_NUMBER = "102";
    private static final String GUEST_1_ID = "G001";
    private static final String GUEST_2_ID = "G002";
    private static final String RECEPTIONIST_ID = "R001";

    private Hotel hotel;
    private RoomFactory roomFactory;
    private Guest guestAlice;
    private Guest guestBob;
    private Room deluxeRoom;
    private Room businessRoom;
    private Date today;
    private Date futureDate;

    @BeforeEach
    void setUp() {
        hotel = new Hotel(HOTEL_NAME);
        roomFactory = new DeluxeRoomFactory();

        deluxeRoom = roomFactory.createRoom(RoomStyle.DELUXE, DELUXE_ROOM_NUMBER, 150.0, false);
        businessRoom = roomFactory.createRoom(RoomStyle.BUSINESS_SUITE, BUSINESS_ROOM_NUMBER, 200.0, false);
        hotel.addRoom(deluxeRoom);
        hotel.addRoom(businessRoom);

        guestAlice = new Guest(GUEST_1_ID, "Alice", "alice@example.com", "111");
        guestBob = new Guest(GUEST_2_ID, "Bob", "bob@example.com", "222");
        hotel.addUser(guestAlice);
        hotel.addUser(guestBob);

        Calendar cal = Calendar.getInstance();
        today = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 5);
        futureDate = cal.getTime();
    }

    @AfterEach
    void tearDown() {
        hotel = null; // Clean up to avoid state leakage
    }

    // --- Helper Methods ---
    private RoomBooking createConfirmedBooking(String reservationNumber, String roomNumber, String guestId, Date date, int days)
            throws InvalidBookingException, RoomNotAvailableException {
        BookRoomCommand cmd = new BookRoomCommand(hotel, reservationNumber, roomNumber, guestId, date, days);
        cmd.execute();
        return hotel.findBookingByReservationNumber(reservationNumber);
    }

    private List<RoomBooking> findBookingsForRoom(Room room) {
        return hotel.bookings.stream()
                .filter(b -> b.getRoom().equals(room))
                .collect(Collectors.toList());
    }

    // --- Tests ---
    @Test
    @DisplayName("1. Add rooms and users successfully")
    void testHotelInitialization() {
        assertEquals(HOTEL_NAME, hotel.getName());
        assertNotNull(hotel.findRoomByNumber(DELUXE_ROOM_NUMBER));
        assertNotNull(hotel.findUserById(GUEST_1_ID));
    }

    @Test
    @DisplayName("2. Book an available room")
    void testSuccessfulBooking() throws Exception {
        RoomBooking booking = createConfirmedBooking("RES_001", DELUXE_ROOM_NUMBER, GUEST_1_ID, futureDate, 2);

        assertBookingDetails(booking, DELUXE_ROOM_NUMBER, GUEST_1_ID, BookingStatus.CONFIRMED);
        assertTrue(guestAlice.getBookings().contains(booking));
    }

    @Test
    @DisplayName("3. Fail to book overlapping dates")
    void testOverlappingBooking() throws Exception {
        createConfirmedBooking("RES_002", DELUXE_ROOM_NUMBER, GUEST_1_ID, futureDate, 3);

        BookRoomCommand overlappingCmd = new BookRoomCommand(
                hotel, "RES_003", DELUXE_ROOM_NUMBER, GUEST_2_ID, futureDate, 2
        );

        assertThrows(RoomNotAvailableException.class, overlappingCmd::execute);
        assertNull(hotel.findBookingByReservationNumber("RES_003"));
    }

    @Test
    @DisplayName("4. Cancel a confirmed booking")
    void testCancelBooking() throws Exception {
        RoomBooking booking = createConfirmedBooking("RES_004", DELUXE_ROOM_NUMBER, GUEST_1_ID, futureDate, 2);

        new CancelBookingCommand(hotel, "RES_004").execute();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertFalse(deluxeRoom.bookings.contains(booking));
    }

    @Test
    @DisplayName("5. Search available rooms by style")
    void testRoomSearchStrategy() throws Exception {
        createConfirmedBooking("RES_005", DELUXE_ROOM_NUMBER, GUEST_1_ID, futureDate, 2);

        List<Room> availableRoomsToday = hotel.searchRooms(
                new RoomAvailabilitySearchStrategy(), RoomStyle.DELUXE, today, 1
        );

        List<Room> availableRoomsFuture = hotel.searchRooms(
                new RoomAvailabilitySearchStrategy(), RoomStyle.DELUXE, futureDate, 1
        );

        assertEquals(1, availableRoomsToday.size());
        assertEquals(0, availableRoomsFuture.size());
    }

    @Test
    @DisplayName("6. Receptionist check-in/check-out flow")
    void testReceptionistWorkflow() throws Exception {
        Receptionist receptionist = new Receptionist(RECEPTIONIST_ID, "John", "john@hotel.com", "333");
        hotel.addUser(receptionist);

        RoomBooking booking = createConfirmedBooking("RES_006", DELUXE_ROOM_NUMBER, GUEST_1_ID, today, 1);

        // Check-in
        receptionist.checkInGuest(booking);
        assertBookingDetails(booking, DELUXE_ROOM_NUMBER, GUEST_1_ID, BookingStatus.CHECKED_IN);
        assertEquals(RoomStatus.OCCUPIED, deluxeRoom.getStatus());

        // Check-out
        receptionist.checkOutGuest(booking);
        assertBookingDetails(booking, DELUXE_ROOM_NUMBER, GUEST_1_ID, BookingStatus.CHECKED_OUT);
        assertEquals(RoomStatus.AVAILABLE, deluxeRoom.getStatus());
    }

    @RepeatedTest(3) // Run multiple times to catch flakiness
    @DisplayName("7. Concurrency: Only one booking succeeds per room")
    void testConcurrentBookings() throws Exception {
        Room concurrentRoom = roomFactory.createRoom(RoomStyle.BUSINESS_SUITE, "CON_001", 250.0, false);
        hotel.addRoom(concurrentRoom);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger successes = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    createConfirmedBooking(
                            "CON_RES_" + Thread.currentThread().getId(),
                            "CON_001",
                            GUEST_1_ID,
                            today,
                            1
                    );
                    successes.incrementAndGet();
                } catch (RoomNotAvailableException ignored) {
                    // Expected for failed bookings
                } catch (Exception e) {
                    fail("Unexpected exception: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, successes.get(), "Only one thread should succeed");
        assertEquals(1, findBookingsForRoom(concurrentRoom).size());
    }

    // --- Custom Assertions ---
    private void assertBookingDetails(RoomBooking booking, String expectedRoomNumber,
                                      String expectedGuestId, BookingStatus expectedStatus) {
        assertNotNull(booking);
        assertEquals(expectedRoomNumber, booking.getRoom().getRoomNumber());
        assertEquals(expectedGuestId, booking.getGuest().getId());
        assertEquals(expectedStatus, booking.getStatus());
    }
}