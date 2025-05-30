package pratikwayase.template;

import pratikwayase.model.Room;



public class FoodService extends RoomServiceTemplate {
    @Override
    protected void checkServiceAvailability() {
        System.out.println("Checking availability for food service");
    }

    @Override
    protected void performService(Room room) {
        System.out.println("Serving food to room: " + room.getRoomNumber());
    }

    @Override
    protected void addServiceCharge(Room room) {
        System.out.println("Adding food service charge to room: " + room.getRoomNumber());
    }
}
