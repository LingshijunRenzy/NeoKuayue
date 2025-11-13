package willow.train.kuayue.systems.train_extension.conductor;

import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.train_extension.TrainAdditionalData;

import java.util.UUID;


public abstract class Conductable {

    private final ConductorType type;

    @Setter
    @NonNull
    private UUID train;

    @Getter
    @Setter
    private int carriage;

    @Getter
    @Setter
    private boolean isLeading;

    @Setter
    @Nullable
    private Conductable connected;

    @Setter
    @Getter
    private int offset = 0;

    @Setter
    @Getter
    private int distanceToAnchor = 0;

    @Setter
    @Getter
    private int priority = 0;

    public Conductable(ConductorType type, @NotNull Train train,
                       Carriage carriage, boolean isLeading) {
        this.type = type;
        this.train = train.id;
        this.carriage = train.carriages.indexOf(carriage);
        this.isLeading = isLeading;
    }
    public Conductable(ConductorType type, GlobalRailwayManager manager, CompoundTag nbt) {
        ConductorLocation selfLoc = new ConductorLocation(nbt.getCompound("self"));
        this.type = type;
        this.train = selfLoc.getTrainId();
        this.carriage = selfLoc.getCarriageIndex();
        this.isLeading = selfLoc.isLeading();
        this.offset = nbt.getInt("offset");
        this.distanceToAnchor = nbt.getInt("distanceToAnchor");
        this.priority = nbt.getInt("priority");
    }


    public boolean free() {
        return connected() == null;
    }

    public abstract boolean valid();

    public boolean canBeConnected() {
        return valid() && free();
    }

    public abstract boolean canConnectTo(Level level,
                         Train otherTrain,
                         Carriage otherCarriage,
                         Conductable other);

    public abstract boolean connect(Level level,
                    Train otherTrain,
                    Carriage otherCarriage,
                    Conductable other);

    public @Nullable Conductable connected() {
        return connected;
    }

    public ConductorType type() {
        return type;
    }

    public UUID train() {
        return train;
    }

    public int carriage() {
        return carriage;
    }

    public ConductorLocation getLoc() {
        return new ConductorLocation(train(),
                carriage(),
                isLeading());
    }

    public void write(CompoundTag nbt) {
        CompoundTag locTag = new CompoundTag();
        getLoc().write(locTag);
        nbt.put("self", locTag);
        nbt.putFloat("offset", offset);
        nbt.putFloat("distanceToAnchor", distanceToAnchor);
        nbt.putInt("priority", priority);
        if (connected != null) {
            CompoundTag connectedTag = new CompoundTag();
            connected.getLoc().write(connectedTag);
            nbt.put("connected", connectedTag);
        }
    }

    public void read(TrainAdditionalData data, CompoundTag nbt) {
        if (!nbt.contains("connected")) return;
        ConductorLocation connectedLoc =
                new ConductorLocation(nbt.getCompound("connected"));
        this.connected = data.getConductorMap().get(connectedLoc);
    }

    public int getTotalOffset() {
        return offset + distanceToAnchor;
    }
}
