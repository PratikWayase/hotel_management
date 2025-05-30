package pratikwayase.template;

import pratikwayase.model.Room;


public abstract class RoomServiceTemplate {
    
    public final void executeService(Room room) {
        System.out.println("\n--- Starting Service for Room " + room.getRoomNumber() + " ---");
        checkServiceAvailability(); 
        performService(room);      
        addServiceCharge(room);    
        System.out.println("--- Service Completed for Room " + room.getRoomNumber() + " ---\n");
    }


    protected abstract void checkServiceAvailability();
    protected abstract void performService(Room room);
    protected abstract void addServiceCharge(Room room);
}
