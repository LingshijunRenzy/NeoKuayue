package willow.train.kuayue.systems.train_extension.conductor;

import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import willow.train.kuayue.initial.AllConductorTypes;
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;

public class Conductor extends Conductable {

    public Conductor(ConductorType type,
                     Train train,
                     Carriage carriage,
                     boolean isLeading) {
        super(type, train, carriage, isLeading);
    }

    public Conductor(ConductorType type, GlobalRailwayManager railway, CompoundTag nbt) {
        super(type, railway, nbt);
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public boolean canConnectTo(Level level,
                                Train otherTrain,
                                Carriage otherCarriage,
                                Conductable other) {
        if(this.type().equals(AllConductorTypes.DUMMY)) return true;
        return other.type().equals(this.type());
    }

    @Override
    public boolean connect(Level level,
                           Train otherTrain,
                           Carriage otherCarriage,
                           Conductable other) {
        return false;
    }

    @Override
    public void read(TrainAdditionalData data, CompoundTag nbt) {}
}
