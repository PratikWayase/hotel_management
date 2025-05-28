package pratikwayase.factory;



import pratikwayase.enums.RoomStyle;
import pratikwayase.model.Room;

public class FamilySuiteRoomFactory extends RoomFactory {
    @Override
    public Room createRoom(RoomStyle style, String roomNumber, double price, boolean isSmoking) {
        return new Room(roomNumber, style, price, isSmoking);
    }
}