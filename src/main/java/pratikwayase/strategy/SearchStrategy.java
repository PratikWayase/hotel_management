package pratikwayase.strategy;

import pratikwayase.enums.RoomStyle;
import pratikwayase.model.Room;
import java.util.*;



public interface SearchStrategy {
    // Parameters now directly used in search logic for more realistic filtering
    List<Room> searchRooms(List<Room> rooms, RoomStyle style, Date startDate, int duration);
}