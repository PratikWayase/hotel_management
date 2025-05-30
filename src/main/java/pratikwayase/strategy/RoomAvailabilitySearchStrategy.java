package pratikwayase.strategy;

import pratikwayase.enums.RoomStyle;
import pratikwayase.model.Room;
import java.util.*;

public class RoomAvailabilitySearchStrategy implements SearchStrategy {
    @Override
    public List<Room> searchRooms(List<Room> rooms, RoomStyle style, Date startDate, int duration) {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            if (room.isRoomAvailable(startDate, duration) && (style == null || room.getStyle() == style)) {
                result.add(room);
            }
        }
        return result;
    }
}
