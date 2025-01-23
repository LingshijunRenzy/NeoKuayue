package willow.train.kuayue.systems.device.driver.path.list;

public interface EdgePathPointListener<T extends EdgePathPoint> {
    default void onUpdateFront(T frontElement) {}
    default void onUpdateBack(T frontElement) {}
    default void onInsertCurrent(T element, int index) {}
    default void onPushCurrent(T element) {}
    default void onRemoveCurrent(T element) {}
    default void onPopCurrent() {}
} 