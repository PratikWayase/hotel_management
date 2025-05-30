package pratikwayase.strategy;

import pratikwayase.enums.RoomStyle;
import pratikwayase.model.Room;
import java.util.*;



public interface SearchStrategy {
    List<Room> searchRooms(List<Room> rooms, RoomStyle style, Date startDate, int duration);
}
