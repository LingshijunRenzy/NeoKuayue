package willow.train.kuayue.systems.train_extension.conductor.schedule_handle;

import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;

public interface ScheduleHandler {

    void attachScheduleToCarriage(Train train, int carriageIndex);
    // @Return: the owner index of the schedule, -1 if no owner
    int getScheduleOwner(Train train);
    void detachSchedule(Train train);
    boolean isScheduleAttached(Train train);

    boolean shouldTransferSchedule(Train train, int carriageIndex);
    void transferSchedule(Train from, Train to, int targetCarriageIndex);

    void handleMerge(Train loco, Train carriages);
    void handleDivide(Train loco, Train newTrain, int splitCarriageIndex);

    default void saveScheduleHolder(Train train, CompoundTag tag) {};
    default void readScheduleHolder(Train train, CompoundTag tag) {};
}
