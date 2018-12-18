package ut.com.cloudlocksdk;

import java.util.HashMap;
import java.util.Map;

public class TimeRecord {

    private static Map<String, Long> map = new HashMap<>();

    public static void start(String tag) {
        map.put(tag, System.currentTimeMillis());
    }

    public static long end(String tag) {
        if (map.get(tag) == null) {
            return -1;
        }

        return System.currentTimeMillis() - map.get(tag);
    }
}
