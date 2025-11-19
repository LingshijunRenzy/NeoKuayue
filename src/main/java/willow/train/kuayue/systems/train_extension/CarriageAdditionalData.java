package willow.train.kuayue.systems.train_extension;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import kasuga.lib.core.util.data_type.Pair;
import lombok.NonNull;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

import java.util.UUID;

public class CarriageAdditionalData {

    public int bogeyCount;

    public int blockCount;

    public BlockPos secondBogeyPos;

    public boolean shouldRemap = false;

    @NonNull
    public Pair<Conductable, Conductable> conductors;

    public boolean valid = true;

    public static CarriageAdditionalData invalid() {
        CarriageAdditionalData data = new CarriageAdditionalData(0, 0, BlockPos.ZERO);
        data.valid = false;
        return data;
    }

    public CarriageAdditionalData(int blockCount, int bogeyCount, BlockPos secondBogeyPos) {
        this.blockCount = blockCount;
        this.secondBogeyPos = secondBogeyPos;
        this.bogeyCount = bogeyCount;
        conductors = Pair.of(null, null);
    }

    public CarriageAdditionalData(int blockCount, int bogeyCount, BlockPos secondBogeyPos,
                                  @NonNull Pair<Conductable, Conductable> conductors) {
        this.blockCount = blockCount;
        this.secondBogeyPos = secondBogeyPos;
        this.bogeyCount = bogeyCount;
        this.conductors = conductors;
    }

    public @Nullable Conductable getFirstConductor() {
        return conductors.getFirst();
    }

    public @Nullable Conductable getSecondConductor() {
        return conductors.getSecond();
    }

    public boolean hasFirstConductor() {
        return conductors.getFirst() != null;
    }

    public boolean hasSecondConductor() {
        return conductors.getSecond() != null;
    }

    public CarriageAdditionalData(CompoundTag nbt) {
        blockCount = nbt.getInt("blockCount");
        bogeyCount = nbt.getInt("bogeyCount");
        secondBogeyPos = new BlockPos(
                nbt.getInt("secondBogeyPosX"),
                nbt.getInt("secondBogeyPosY"),
                nbt.getInt("secondBogeyPosZ")
        );
        Conductable first = null, second = null;
        if (nbt.contains("conductor1")) {
            CompoundTag conductor1 = nbt.getCompound("conductor1");
            ConductorType type = Kuayue.TRAIN_EXTENSION.getType(new ResourceLocation(conductor1.getString("type")));
            if (type != null) {
                first = type.build(Create.RAILWAYS, conductor1);
            }
        }
        if (nbt.contains("conductor2")) {
            CompoundTag conductor1 = nbt.getCompound("conductor2");
            ConductorType type = Kuayue.TRAIN_EXTENSION.getType(new ResourceLocation(conductor1.getString("type")));
            if (type != null) {
                second = type.build(Create.RAILWAYS, conductor1);
            }
        }
        this.conductors = Pair.of(first, second);
        shouldRemap = nbt.getBoolean("shouldRemap");
    }

    public void write(CompoundTag nbt) {
        nbt.putInt("blockCount", blockCount);
        nbt.putInt("bogeyCount", bogeyCount);
        nbt.putInt("secondBogeyPosX", secondBogeyPos.getX());
        nbt.putInt("secondBogeyPosY", secondBogeyPos.getY());
        nbt.putInt("secondBogeyPosZ", secondBogeyPos.getZ());
        if (conductors.getFirst() != null) {
            CompoundTag conductorTag = new CompoundTag();
            conductorTag.putString("type", conductors.getFirst().type().id().toString());
            conductors.getFirst().write(conductorTag);
            nbt.put("conductor1", conductorTag);
        }
        if (conductors.getSecond() != null) {
            CompoundTag conductorTag = new CompoundTag();
            conductorTag.putString("type", conductors.getSecond().type().id().toString());
            conductors.getSecond().write(conductorTag);
            nbt.put("conductor2", conductorTag);
        }
        nbt.putBoolean("shouldRemap", shouldRemap);
    }

    public void reverse(Carriage carriage) {
        Contraption contraption = carriage.anyAvailableEntity().getContraption();
        if(!(contraption instanceof CarriageContraption cc)) return;

        if (this.shouldRemap){
            this.secondBogeyPos = bogeyCount > 1 ?
                    BlockPos.ZERO.relative(cc.getAssemblyDirection().getOpposite(), carriage.bogeySpacing) :
                    BlockPos.ZERO;
        } else {
            this.secondBogeyPos = bogeyCount > 1 ?
                    BlockPos.ZERO.relative(cc.getAssemblyDirection(), carriage.bogeySpacing) :
                    BlockPos.ZERO;
        }

        Conductable first = conductors.getFirst();
        Conductable second = conductors.getSecond();

        conductors = Pair.of(second, first);

        if(first != null) {
            first.setLeading(false);
        }
        if(second != null) {
            second.setLeading(true);
        }
    }

    public void setTrainInfo(UUID trainId, int carriageIndex) {
        Conductable first = conductors.getFirst();
        Conductable second = conductors.getSecond();
        if (first != null) {
            first.setTrain(trainId);
            first.setCarriage(carriageIndex);
        }
        if (second != null) {
            second.setTrain(trainId);
            second.setCarriage(carriageIndex);
        }
    }
}
