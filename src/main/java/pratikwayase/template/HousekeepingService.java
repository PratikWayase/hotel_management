package pratikwayase.template;

import pratikwayase.model.Room;


public class HousekeepingService extends RoomServiceTemplate {
    @Override
    protected void checkServiceAvailability() {
        System.out.println("Checking availability for housekeeping service (e.g., staff, time slots).");
    }

    @Override
    protected void performService(Room room) {
        System.out.println("Performing housekeeping for room: " + room.getRoomNumber() + " (cleaning, tidying).");
    }

    @Override
    protected void addServiceCharge(Room room) {
        System.out.println("Adding housekeeping charge to room: " + room.getRoomNumber() + " (if applicable).");
    }
}