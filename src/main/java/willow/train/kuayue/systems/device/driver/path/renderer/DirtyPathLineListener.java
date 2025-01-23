package willow.train.kuayue.systems.device.driver.path.renderer;

import willow.train.kuayue.systems.device.driver.path.list.EdgePathLine;
import willow.train.kuayue.systems.device.driver.path.list.EdgePathLineListener;

public interface DirtyPathLineListener<T extends EdgePathLine> extends EdgePathLineListener<T> {

    void markDirty();

    @Override
    default void onInsertFront(T element) {
        markDirty();
    }

    @Override
    default void onInsertBack(T element) {
        markDirty();
    }

    @Override
    default void onRemoveFront(T element) {
        markDirty();
    }

    @Override
    default void onRemoveBack(T element) {
        markDirty();
    }

    @Override
    default void onInsertCurrent(T element) {
        markDirty();
    }

    @Override
    default void onRemoveCurrent(T element) {
        markDirty();
    }

    @Override
    default void onPopCurrent() {
        markDirty();
    }
}
