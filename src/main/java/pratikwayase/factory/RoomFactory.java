package pratikwayase.factory;


import pratikwayase.enums.RoomStyle;
import pratikwayase.model.Room;

public abstract class RoomFactory {
    public abstract Room createRoom(RoomStyle style, String roomNumber, double price, boolean isSmoking);
}
