package pratikwayase.template;

import pratikwayase.model.Room;



public class FoodService extends RoomServiceTemplate {
    @Override
    protected void checkServiceAvailability() {
        System.out.println("Checking availability for food service (e.g., kitchen status, menu items).");
    }

    @Override
    protected void performService(Room room) {
        System.out.println("Serving food to room: " + room.getRoomNumber() + " (delivery of order).");
    }

    @Override
    protected void addServiceCharge(Room room) {
        System.out.println("Adding food service charge to room: " + room.getRoomNumber() + " (for items ordered).");
    }
}