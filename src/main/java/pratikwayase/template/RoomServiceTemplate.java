package pratikwayase.template;

import pratikwayase.model.Room;


public abstract class RoomServiceTemplate {
    // The final template method that defines the algorithm's structure
    public final void executeService(Room room) {
        System.out.println("\n--- Starting Service for Room " + room.getRoomNumber() + " ---");
        checkServiceAvailability(); // Step 1: Hook method (implemented by subclasses)
        performService(room);       // Step 2: Hook method (implemented by subclasses)
        addServiceCharge(room);     // Step 3: Hook method (implemented by subclasses)
        System.out.println("--- Service Completed for Room " + room.getRoomNumber() + " ---\n");
    }

    // Abstract methods (hooks) that must be implemented by concrete subclasses
    protected abstract void checkServiceAvailability();
    protected abstract void performService(Room room);
    protected abstract void addServiceCharge(Room room);
}