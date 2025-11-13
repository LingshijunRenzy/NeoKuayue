package willow.train.kuayue.systems.train_extension;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.entity.Train;
import kasuga.lib.core.base.NbtSerializable;
import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorLocation;

import java.util.*;

public class TrainAdditionalData implements NbtSerializable {

    @Getter
    private final UUID train;

    @Getter
    private final List<CarriageAdditionalData> carriages;

    @Getter
    private final HashMap<ConductorLocation, Conductable> conductorMap;

    public TrainAdditionalData(Train train) {
        this(train, new ArrayList<>());
    }

    public TrainAdditionalData(CompoundTag nbt) {
        train = nbt.getUUID("trainId");
        carriages = new ArrayList<>();
        conductorMap = new HashMap<>();
        read(nbt);
    }

    public TrainAdditionalData(Train train,
                               List<CarriageAdditionalData> carriages) {
        this.train = train.id;
        this.carriages = carriages;
        conductorMap = new HashMap<>();
        carriages.forEach(data -> {
            Pair<Conductable, Conductable> pair = data.conductors;
            if (data.conductors.getFirst() != null) {
                conductorMap.put(pair.getFirst().getLoc(), pair.getFirst());
            }
            if (pair.getSecond() != null) {
                conductorMap.put(pair.getSecond().getLoc(), pair.getSecond());
            }
        });
    }

    // notice: 获取当前车上两端的可用车钩
    public @NotNull Pair<Conductable, Conductable> getSidedConductors() {
        if (carriages.isEmpty()) return Pair.of(null, null);
        Pair<Conductable, Conductable> firstCarriageConductors = carriages.get(0).conductors;
        Conductable head = firstCarriageConductors.getFirst();
        Pair<Conductable, Conductable> lastCarriageConductors = carriages.get(carriages.size() - 1).conductors;
        return Pair.of(head, lastCarriageConductors.getSecond());
    }

    @Override
    public void write(CompoundTag nbt) {
        nbt.putUUID("trainId", train);
        for (int i = 0; i < carriages.size(); i++) {
            CarriageAdditionalData data = carriages.get(i);
            CompoundTag tag = new CompoundTag();
            data.write(tag);
            nbt.put("conductor" + i, tag);
        }
    }

    @Override
    public void read(CompoundTag nbt) {
        int i = 0;

        // 设置 connection
        while (nbt.contains("conductor" + i)) {
            CompoundTag tag = nbt.getCompound("conductor" + i);
            i++;
            CarriageAdditionalData data = new CarriageAdditionalData(tag);
            carriages.add(data);
            if (data.hasFirstConductor()) {
                conductorMap.put(data.getFirstConductor().getLoc(), data.getFirstConductor());
            }
            if (data.hasSecondConductor()) {
                conductorMap.put(data.getSecondConductor().getLoc(), data.getSecondConductor());
            }
        }
    }

    public void addConductorPair(int blockCount, int bogeyCount,
                                 BlockPos secondBogeyPos,
                                 Pair<Conductable, Conductable> pair) {
        this.carriages.add(new CarriageAdditionalData(blockCount, bogeyCount, secondBogeyPos, pair));
        if (pair.getFirst() != null) {
            conductorMap.put(pair.getFirst().getLoc(), pair.getFirst());
        }
        if (pair.getSecond() != null) {
            conductorMap.put(pair.getSecond().getLoc(), pair.getSecond());
        }
    }

    public void updateInternalConnections() {
        for(int i = 0; i < carriages.size() - 1; i++) {
            Pair<Conductable, Conductable> pair = carriages.get(i).conductors;
            Pair<Conductable, Conductable> nextPair = carriages.get(i + 1).conductors;
            if(i == 0 && pair.getFirst() != null) {
                pair.getFirst().setConnected(null);
            }
            if(i == carriages.size() - 2 && nextPair.getSecond() != null) {
                nextPair.getSecond().setConnected(null);
            }

            if (pair.getSecond() == null || nextPair.getFirst() == null) {
                continue;
            }
            pair.getSecond().setConnected(nextPair.getFirst());
            nextPair.getFirst().setConnected(pair.getSecond());
        }
    }

    public float totalWeight() {
        float result = 0;
        int i = 0;
        Train t = Create.RAILWAYS.trains.get(train);
        for (CarriageAdditionalData data : carriages) {
            result += ExtensionHelper.getDynamicWeight(t.carriages.get(i), data.blockCount);
            i++;
        }
        return result;
    }

    public @Nullable Conductable getConductor(byte index) {
        if (index == 0 || carriages.isEmpty()) return null;
        return index > 0 ?
                carriages.get(0).getFirstConductor() :
                carriages.get(carriages.size() - 1).getSecondConductor();
    }

    public void updateConductorMap() {
        this.conductorMap.clear();
        for (CarriageAdditionalData data : carriages) {
            Pair<Conductable, Conductable> pair = data.conductors;
            if (data.conductors.getFirst() != null) {
                conductorMap.put(pair.getFirst().getLoc(), pair.getFirst());
            }
            if (pair.getSecond() != null) {
                conductorMap.put(pair.getSecond().getLoc(), pair.getSecond());
            }
        }
    }

    //call this after reversing the Train carriages list
    public void reverse(@NonNull Train t){

        // reverse carriages
        Collections.reverse(carriages);
        for(int i = 0; i < carriages.size(); i++) {
            carriages.get(i).reverse(t.carriages.get(i));
        }
        reIndexAll(t);
        updateConductorMap();
    }

    public void reIndexAll(@NonNull Train t) {
        for(int i = 0; i < carriages.size(); i++) {
            carriages.get(i).setTrainInfo(t.id, i);
        }
    }
}
