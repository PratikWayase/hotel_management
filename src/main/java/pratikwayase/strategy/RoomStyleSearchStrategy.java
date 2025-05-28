package pratikwayase.strategy;

import pratikwayase.enums.RoomStyle;
import pratikwayase.model.Room;
import java.util.*;


public class RoomStyleSearchStrategy implements SearchStrategy {
    @Override
    public List<Room> searchRooms(List<Room> rooms, RoomStyle style, Date startDate, int duration) {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            // Check for both style and actual availability for the given dates
            // room.isRoomAvailable() handles its own internal locking.
            if (room.getStyle() == style && room.isRoomAvailable(startDate, duration)) {
                result.add(room);
            }
        }
        return result;
    }
}