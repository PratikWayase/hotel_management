package pratikwayase.strategy;

import pratikwayase.enums.RoomStyle;
import pratikwayase.model.Room;
import java.util.*;


public class RoomStyleSearchStrategy implements SearchStrategy {
    @Override
    public List<Room> searchRooms(List<Room> rooms, RoomStyle style, Date startDate, int duration) {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getStyle() == style && room.isRoomAvailable(startDate, duration)) {
                result.add(room);
            }
        }
        return result;
    }
}
