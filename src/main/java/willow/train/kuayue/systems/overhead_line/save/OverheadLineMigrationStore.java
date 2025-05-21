package willow.train.kuayue.systems.overhead_line.save;

import com.simibubi.create.content.trains.graph.DimensionPalette;
import kasuga.lib.core.create.boundary.ResourcePattle;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;
import com.mojang.math.Vector3f;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;
import willow.train.kuayue.systems.overhead_line.wire.WireReg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OverheadLineMigrationStore extends SavedData {
    // For further backward compatibility

    public record Node(
            ResourceLocation type,
            List<OverheadLineSupportBlockEntity.Connection> connections
    ){}

    public final HashMap<Pair<ResourceKey<Level>, BlockPos>, Node> nodeConnections = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ResourcePattle palette = new ResourcePattle();
        DimensionPalette dimensionPalette = new DimensionPalette();
        ListTag positionTags = new ListTag();

        nodeConnections.forEach((blockPos, connectionList) -> {
            CompoundTag posTag = new CompoundTag();
            posTag.put("Pos", NbtUtils.writeBlockPos(blockPos.getSecond()));
            posTag.putInt("Dimension", dimensionPalette.encode(blockPos.getFirst()));
            posTag.putInt("Type", palette.encode(connectionList.type()));
            
            ListTag connectionTags = new ListTag();
            for (OverheadLineSupportBlockEntity.Connection connection : connectionList.connections()) {
                CompoundTag connectionTag = new CompoundTag();
                connectionTag.put("absolutePos", NbtUtils.writeBlockPos(connection.absolutePos()));
                connectionTag.put("relativePos", NbtUtils.writeBlockPos(connection.relativePos()));
                connectionTag.putInt("type", palette.encode(WireReg.getName(connection.type())));
                connectionTag.putInt("index", connection.connectionIndex());
                connectionTag.putInt("targetIndex", connection.targetIndex());
                connectionTag.putFloat("tX", connection.toPosition().x());
                connectionTag.putFloat("tY", connection.toPosition().y());
                connectionTag.putFloat("tZ", connection.toPosition().z());
                connectionTags.add(connectionTag);
            }
            posTag.put("Connections", connectionTags);
            positionTags.add(posTag);
        });
        
        pCompoundTag.put("BlockConnections", positionTags);
        CompoundTag paletteTag = new CompoundTag();
        palette.write(paletteTag);
        pCompoundTag.put("ResourcePalette", paletteTag);
        CompoundTag dimensionPaletteTag = new CompoundTag();
        dimensionPalette.write(dimensionPaletteTag);
        pCompoundTag.put("DimensionPalette", dimensionPaletteTag);
        return pCompoundTag;
    }

    public static OverheadLineMigrationStore load(CompoundTag pCompoundTag) {
        OverheadLineMigrationStore store = new OverheadLineMigrationStore();
        if (!pCompoundTag.contains("BlockConnections")) return store;
        
        ResourcePattle palette = ResourcePattle.read(pCompoundTag.getCompound("ResourcePalette"));
        DimensionPalette dimensionPalette = DimensionPalette.read(pCompoundTag.getCompound("DimensionPalette"));
        ListTag positionTags = pCompoundTag.getList("BlockConnections", Tag.TAG_COMPOUND);
        
        for (int i = 0; i < positionTags.size(); i++) {
            CompoundTag posTag = positionTags.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(posTag.getCompound("Pos"));
            ListTag connectionTags = posTag.getList("Connections", Tag.TAG_COMPOUND);
            List<OverheadLineSupportBlockEntity.Connection> connectionList = new ArrayList<>();
            
            for (int j = 0; j < connectionTags.size(); j++) {
                CompoundTag connectionTag = connectionTags.getCompound(j);
                BlockPos absolutePos = NbtUtils.readBlockPos(connectionTag.getCompound("absolutePos"));
                BlockPos relativePos = NbtUtils.readBlockPos(connectionTag.getCompound("relativePos"));

                ResourceLocation connectionType = palette.decode(connectionTag.getInt("type"));
                OverheadLineType overheadLineType = WireReg.get(connectionType);
                if(overheadLineType == null) {
                    Kuayue.LOGGER.warn("Unknown connection type: " + connectionType);
                    continue;
                }
                
                connectionList.add(new OverheadLineSupportBlockEntity.Connection(
                    absolutePos,
                    relativePos,
                    overheadLineType,
                    connectionTag.getInt("index"),
                    connectionTag.getInt("targetIndex"),
                    new Vector3f(
                        connectionTag.getFloat("tX"),
                        connectionTag.getFloat("tY"),
                        connectionTag.getFloat("tZ")
                    )
                ));
            }
            store.nodeConnections.put(Pair.of(dimensionPalette.decode(posTag.getInt("Dimension")), pos), new Node(
                palette.decode(posTag.getInt("Type")),
                connectionList
            ));
        }
        
        return store;
    }

    public void setConnectionNode(Level level, BlockPos pos, ResourceLocation type, List<OverheadLineSupportBlockEntity.Connection> connection) {
        nodeConnections.put(Pair.of(level.dimension(), pos), new Node(type, connection));
        setDirty();
    }

    public void removeConnectionNode(Level level, BlockPos blockPos, ResourceLocation key) {
        nodeConnections.remove(Pair.of(level.dimension(), blockPos));
        setDirty();
    }
}
