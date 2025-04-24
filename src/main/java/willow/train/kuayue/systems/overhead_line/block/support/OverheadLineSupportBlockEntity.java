package willow.train.kuayue.systems.overhead_line.block.support;

import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import kasuga.lib.core.create.boundary.ResourcePattle;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.OverheadLineSystem;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineBlockDynamicConfiguration;

import java.util.*;
import java.util.function.Supplier;

public class OverheadLineSupportBlockEntity extends SmartBlockEntity {

    public record Connection(
            BlockPos absolutePos,
            BlockPos relativePos,
            ResourceLocation type,
            int connectionIndex,
            int targetIndex,
            Vector3f toPosition
    ){

        public Connection(
                BlockPos absolutePos,
                BlockPos relativePos,
                ResourceLocation type,
                int connectionIndex,
                int targetIndex,
                Vector3f toPosition
        ) {
            this.absolutePos = absolutePos;
            this.relativePos = relativePos;
            this.type = type;
            this.connectionIndex = connectionIndex;
            this.targetIndex = targetIndex;
            this.toPosition = toPosition;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Connection that)) return false;
            return connectionIndex == that.connectionIndex && Objects.equals(absolutePos, that.absolutePos) && Objects.equals(relativePos, that.relativePos) && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(absolutePos, relativePos, type, connectionIndex);
        }
    };

    public static final HashMap<Block, OverheadLineBlockDynamicConfiguration> CONNECTION_POINTS_SUPPLIERS = new HashMap<>();

    private static final HashMap<Supplier<Block>, OverheadLineBlockDynamicConfiguration> map = new HashMap<>();
    public static void registerPoint(Supplier<Block> block, OverheadLineBlockDynamicConfiguration configuration){
        map.put(block, configuration);
    }

    public static void applyRegistration(){
        for (Map.Entry<Supplier<Block>, OverheadLineBlockDynamicConfiguration> entry : map.entrySet()) {
            Supplier<Block> block = entry.getKey();
            OverheadLineBlockDynamicConfiguration configuration = entry.getValue();
            CONNECTION_POINTS_SUPPLIERS.put(block.get(), configuration);
        }
    }

    public OverheadLineSupportBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        configuration = CONNECTION_POINTS_SUPPLIERS.get(this.getBlockState().getBlock());
    }

    public OverheadLineSupportBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(OverheadLineSystem.OVERHEAD_LINE_SUPPORT_BLOCK_ENTITY.getType(), blockPos, blockState);
        configuration = CONNECTION_POINTS_SUPPLIERS.get(this.getBlockState().getBlock());
    }

    protected final OverheadLineBlockDynamicConfiguration configuration;

    List<Connection> connections = new ArrayList<>();

    float rotation = 0f;
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {}

    public Optional<String> checkConnectable(OverheadLineSupportBlockEntity targetSupport) {
        return Optional.empty();
    }

    public float getRotation() {
        return (- this.getBlockState().getValue(OverheadLineSupportBlock.FACING).getOpposite().toYRot() - 90 + this.rotation);
    }

    public static final Vec3 BASIC_OFFSET = new Vec3(.5, 0, .5);

    public int getConnectionIndexOf(Vec3 eyePosition){
        float rot = (float) Math.toRadians(getRotation());
        List<Vec3> conPos = this.getConnectionPoints();
        BlockPos pPos = this.getBlockPos();
        if(conPos.size() > 1){
            int closest = 0;
            double distance = conPos.get(0).yRot(rot)
                    .add(pPos.getX(), pPos.getY(), pPos.getZ())
                    .add(BASIC_OFFSET).distanceTo(eyePosition);
            for(int i = 1; i < conPos.size(); i++) {
                double dis_temp = conPos.get(i).yRot(rot)
                        .add(pPos.getX(), pPos.getY(), pPos.getZ())
                        .add(BASIC_OFFSET).distanceTo(eyePosition);
                if(distance > dis_temp) {
                    distance = dis_temp;
                    closest = i;
                }
            }
            return closest;
        } else {
            return 0;
        }
    }

    public List<Vec3> getActualConnectionPoints() {
        List<Vec3> conPos = this.getConnectionPoints();
        BlockPos pPos = this.getBlockPos();
        float rot = (float) Math.toRadians(getRotation());
        List<Vec3> actualConPos = new ArrayList<>();
        for(Vec3 pos : conPos) {
            actualConPos.add(pos.yRot(rot).add(pPos.getX(), pPos.getY(), pPos.getZ()).add(BASIC_OFFSET));
       }
        return actualConPos;
    }

    public Vec3 getConnectionPointByIndex(int index){
        return index >= getConnectionPoints().size() ? Vec3.atCenterOf(getBlockPos()) : getActualConnectionPoints().get(index);
    }

    private List<Vec3> getConnectionPoints() {
        return configuration.connectionPoints();
    }

    public void addConnection(
            BlockPos target,
            ResourceLocation itemType,
            int thisConnectionIndex,
            int targetConnectionIndex,
            OverheadLineSupportBlockEntity targetBlockEntity
    ) {
        BlockPos thisPos = this.getBlockPos();
        this.connections.add(
                new Connection(
                        target,
                        target.subtract(thisPos),
                        itemType,
                        thisConnectionIndex,
                        targetConnectionIndex,
                        new Vector3f(targetBlockEntity.getConnectionPointByIndex(targetConnectionIndex))
                )
        );
        this.notifyUpdate();
        onConnectionModification();
    }

    public void removeAllConnections(){
        List<Connection> $connections = List.copyOf(this.connections);
        for (Connection connection : $connections) {
            removeConnection(connection.absolutePos());
        }
    }

    public void removeConnection(BlockPos target) {
        if(this.level != null && this.level.getBlockEntity(target) instanceof OverheadLineSupportBlockEntity targetBlockEntity) {
            targetBlockEntity.notifyRemoveConnection(this.getBlockPos());
        }
        this.notifyRemoveConnection(target);
    }

    public void notifyRemoveConnection(BlockPos from){
        connections.removeIf(connection -> connection.absolutePos().equals(from));
        this.notifyUpdate();
        onConnectionModification();
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        ResourcePattle palette = ResourcePattle.read(tag.getCompound("ResourcePalette"));
        if(tag.contains("connections")) {
            connections = new ArrayList<>();
            if(clientPacket) {
                ListTag connectionTags = tag.getList("connections", Tag.TAG_COMPOUND);
                for(int i = 0; i < connectionTags.size(); i++) {
                    CompoundTag connectionTag = connectionTags.getCompound(i);
                    BlockPos absolutePosition = NbtUtils.readBlockPos(connectionTag.getCompound("absolutePos"));
                    connections.add(new Connection(
                            absolutePosition,
                            NbtUtils.readBlockPos(connectionTag.getCompound("absolutePos")).subtract(this.getBlockPos()),
                            palette.decode(connectionTag.getInt("type")),
                            connectionTag.getInt("index"),
                            connectionTag.getInt("targetIndex"),
                            new Vector3f(connectionTag.getFloat("tX"), connectionTag.getFloat("tY"), connectionTag.getFloat("tZ"))
                    ));
                }
            } else {
                ListTag connectionTags = tag.getList("connections", Tag.TAG_COMPOUND);
                for(int i = 0; i < connectionTags.size(); i++) {
                    CompoundTag connectionTag = connectionTags.getCompound(i);
                    BlockPos absolutePos = NbtUtils.readBlockPos(connectionTag.getCompound("absolutePos"));
                    BlockPos relativePos = NbtUtils.readBlockPos(connectionTag.getCompound("relativePos"));
                    // If the relative position is not the same as the absolute position, then follow the relative position
                    if(!absolutePos.subtract(this.getBlockPos()).equals(relativePos)) {
                        absolutePos = relativePos.offset(this.getBlockPos());
                    }
                    connections.add(new Connection(
                            absolutePos,
                            relativePos,
                            palette.decode(connectionTag.getInt("type")),
                            connectionTag.getInt("index"),
                            connectionTag.getInt("targetIndex"),
                            new Vector3f(connectionTag.getFloat("tX"), connectionTag.getFloat("tY"), connectionTag.getFloat("tZ"))
                    ));
                }
            }
        }
        onConnectionModification();
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        ResourcePattle palette = new ResourcePattle();
        ListTag connectionTags = new ListTag();
        for(Connection connection : connections) {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.put("absolutePos", NbtUtils.writeBlockPos(connection.absolutePos()));
            if(!clientPacket){
                connectionTag.put("relativePos", NbtUtils.writeBlockPos(connection.relativePos()));
            }
            connectionTag.putInt("type", palette.encode(connection.type()));
            connectionTag.putInt("index", connection.connectionIndex());
            connectionTag.putInt("targetIndex", connection.targetIndex());

            connectionTag.putFloat("tX", (float) connection.toPosition.x());
            connectionTag.putFloat("tY", (float) connection.toPosition.y());
            connectionTag.putFloat("tZ", (float) connection.toPosition.z());

            connectionTags.add(connectionTag);
        }
        tag.put("connections", connectionTags);
        CompoundTag paletteTag = new CompoundTag();
        palette.write(paletteTag);
        tag.put("ResourcePalette", paletteTag);
    }

    @Override
    public void destroy() {
        super.destroy();
        removeAllConnections();
    }

    @Override
    public void onChunkUnloaded() {
        if(this.level == null || !this.level.isClientSide)
            return;
    }

    public List<Connection> getConnections(){
        return connections;
    }

    public void onConnectionModification(){}

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-200000, -200000,-200000), getBlockPos().offset(200000,200000,200000));
    }

}
