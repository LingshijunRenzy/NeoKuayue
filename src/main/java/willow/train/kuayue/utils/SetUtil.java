package willow.train.kuayue.utils;

import java.util.Set;

public class SetUtil {
    public static <T> boolean isEquals(Set<T> a, Set<T> b) {
        return a == b || (a != null && b != null && a.size() == b.size() && a.containsAll(b));
    }
}
