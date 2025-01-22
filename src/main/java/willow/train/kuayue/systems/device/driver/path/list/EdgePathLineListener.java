package willow.train.kuayue.systems.device.driver.path.list;

public interface EdgePathLineListener<T extends EdgePathLine> {
    default void onInsertFront(T element) {}
    default void onInsertBack(T element) {}
    default void onRemoveFront(T element) {}
    default void onRemoveBack(T element) {}
    default void onInsertCurrent(T element) {}
    default void onRemoveCurrent(T element) {}
    default void onPopCurrent() {}
} 