package willow.train.kuayue.systems.overhead_line.block.support;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import kasuga.lib.core.create.boundary.ResourcePattle;
import kasuga.lib.core.util.Envs;
import kasuga.lib.core.util.data_type.Pair;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.overhead_line.OverheadLineSystem;
import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRendererBridge;
import willow.train.kuayue.systems.overhead_line.block.line.OverheadLineRendererSystem;
import willow.train.kuayue.systems.overhead_line.block.support.variants.OverheadLineBlockDynamicConfiguration;
import willow.train.kuayue.systems.overhead_line.client.OverheadLineSupportAdjustMenu;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;
import willow.train.kuayue.systems.overhead_line.wire.WireReg;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class OverheadLineSupportBlockEntity extends SmartBlockEntity implements MenuProvider {

    public record Connection(
            BlockPos absolutePos,
            BlockPos relativePos,
            OverheadLineType type,
            int connectionIndex,
            int targetIndex,
            Vector3f toPosition
    ){

        public Connection(
                BlockPos absolutePos,
                BlockPos relativePos,
                OverheadLineType type,
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

        public Connection withBlockEntityPosition(BlockPos bePosition, Vector3f toPosition){
            return new Connection(
                    bePosition.offset(relativePos),
                    relativePos,
                    type,
                    connectionIndex,
                    targetIndex,
                    toPosition
            );
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
        if(Envs.isDevEnvironment()){
            setLazyTickRate(20);
        }
    }

    public OverheadLineSupportBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(OverheadLineSystem.OVERHEAD_LINE_SUPPORT_BLOCK_ENTITY.getType(), blockPos, blockState);
        configuration = CONNECTION_POINTS_SUPPLIERS.get(this.getBlockState().getBlock());
    }

    protected List<Vec3> connectionPoints = List.of();

    @Override
    public void initialize() {
        super.initialize();
        this.connectionPoints = configuration.connectionPoints().get(this.level, this.getBlockPos(), this.getBlockState());
        onConnectionModification();
    }

    protected final OverheadLineBlockDynamicConfiguration configuration;

    List<Connection> connections = new ArrayList<>();

    @Getter
    float rotation = 0f;
    protected float x_offset = 0.0f;
    protected float y_offset = 0.0f;
    protected float z_offset = 0.0f;
    
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {}

    public Optional<String> checkConnectable(OverheadLineSupportBlockEntity targetSupport) {
        if(connections.size() >= configuration.maxConnections()){
            return Optional.of("overhead_line_max_connections_reached");
        }

        BlockPos targetPos = targetSupport.getBlockPos();
        boolean duplicateExists = connections.stream()
                .anyMatch(connection -> connection.absolutePos().equals(targetPos));
        if(duplicateExists) {
            return Optional.of("overhead_line_duplicate_connection");
        }

        return Optional.empty();
    }

    public boolean updateConnectionToPosition() {
        boolean updated = false;
        if(level == null) {
            return false;
        }

        for (int i = 0; i < connections.size(); i++) {
            Connection connection = connections.get(i);
            BlockEntity targetEntity = level.getBlockEntity(connection.absolutePos());
            if(targetEntity == null) {
                continue;
            }

            if(targetEntity instanceof OverheadLineSupportBlockEntity targetSupport) {
                int index = IntStream.range(0, targetSupport.connections.size())
                        .filter(j -> Objects.equals(targetSupport.connections.get(j).absolutePos, this.getBlockPos()))
                        .findFirst()
                        .orElse(-1);
                
                if (index == -1) {
                    continue;
                }

                Connection old = targetSupport.connections.get(index);
                Vec3 newToPosition = this.getConnectionPointByIndex(old.targetIndex(), old.type());

                Connection newConnection = new Connection(
                        old.absolutePos(),
                        old.relativePos(),
                        old.type(),
                        old.connectionIndex(),
                        old.targetIndex(),
                        new Vector3f(newToPosition)
                );

                targetSupport.connections.set(index, newConnection);
                targetSupport.onConnectionPositionUpdated(newConnection, true);
                
                if (level.isClientSide) {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                        OverheadLineRendererBridge.setBlockEntity(targetSupport, targetSupport.connections);
                    });
                }
                updated = true;
            }
        }

        return updated;
    }

    protected void onConnectionPositionUpdated(Connection updatedConnection, boolean fromExternal) {
        this.notifyUpdate();
    }

    public static final Vec3 BASIC_OFFSET = new Vec3(.5, 0, .5);

    public int getConnectionIndexOf(Vec3 eyePosition){
        List<Vec3> actualConnections = this.getActualConnectionPoints();
        if(actualConnections.size() > 1){
            int closest = 0;
            double distance = actualConnections.get(0).distanceTo(eyePosition);
            for(int i = 1; i < actualConnections.size(); i++) {
                double dis_temp = actualConnections.get(i).distanceTo(eyePosition);
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
        List<Vec3> localPoints = this.getConnectionPoints();
        BlockPos pPos = this.getBlockPos();
        float manualDeg = getRotation();

        PoseStack pose = new PoseStack();
        pose.translate(pPos.getX(), pPos.getY(), pPos.getZ());
        pose.translate(BASIC_OFFSET.x, BASIC_OFFSET.y, BASIC_OFFSET.z);

        if (getBlockState().hasProperty(OverheadLineSupportBlock.FACING)) {
            Direction facing = getBlockState().getValue(OverheadLineSupportBlock.FACING);
            pose.mulPose(facing.getRotation());
            pose.mulPose(new Quaternion(-90, -90, 0, true));
        }

        pose.mulPose(Vector3f.YP.rotationDegrees(manualDeg * 1.03f));
        float xOff = this.x_offset;
        float yOff = this.y_offset;
        float zOff = this.z_offset;
        pose.translate(
                -xOff * 1.3f,
                yOff * 1.3f,
                -zOff * 1.3f);

        Matrix4f m = pose.last().pose();
        List<Vec3> worldPoints = new ArrayList<>(localPoints.size());
        for (Vec3 lp : localPoints) {
            pose.pushPose();
            pose.translate(-lp.x, lp.y, -lp.z);
            Matrix4f tm = pose.last().pose();
            Vector4f origin = new Vector4f(0f, 0f, 0f, 1f);
            origin.transform(tm);
            Vec3 worldPoint = new Vec3(origin.x(), origin.y(), origin.z());
            worldPoints.add(worldPoint);
            pose.popPose();
        }

        return worldPoints;
    }

    public Vec3 getConnectionPointByIndex(int index){
        List<Vec3> connectionPoints = getConnectionPoints();
        Vec3 result = index >= connectionPoints.size() ? Vec3.atCenterOf(getBlockPos()) : getActualConnectionPoints().get(index);
        return result;
    }

    public Vec3 getConnectionPointByIndex(int index, OverheadLineType wireType) {
        return getConnectionPointByIndex(index);
    }


    public List<Vec3> getConnectionPoints() {
        return connectionPoints;
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
                        WireReg.get(itemType),
                        thisConnectionIndex,
                        targetConnectionIndex,
                        new Vector3f(targetBlockEntity.getConnectionPointByIndex(targetConnectionIndex, WireReg.get(itemType)))
                )
        );
        this.notifyUpdate();
        onConnectionModification();
    }

    public Pair<Boolean,Optional<Connection>> getFreshConnection(Connection connection) {
        BlockPos thisPos = this.getBlockPos();
        Level level = this.getLevel();
        if(level == null)
            return Pair.of(false, Optional.empty());
        BlockPos newAbsolutePos = thisPos.offset(connection.relativePos());
        BlockEntity targetBlockEntity = level.getBlockEntity(newAbsolutePos);

        if(!(targetBlockEntity instanceof OverheadLineSupportBlockEntity targetOverhead)) {
            return Pair.of(true, Optional.empty());
        }

        return Pair.of(true, Optional.of(connection.withBlockEntityPosition(
                newAbsolutePos,
                new Vector3f(targetOverhead.getConnectionPointByIndex(connection.targetIndex(), connection.type()))
        )));
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

        this.x_offset = tag.getFloat("x_offset");
        this.y_offset = tag.getFloat("y_offset");
        this.z_offset = tag.getFloat("z_offset");

        if (tag.contains("rotation")) {
            this.rotation = tag.getFloat("rotation");
        }
        
        ResourcePattle palette = ResourcePattle.read(tag.getCompound("ResourcePalette"));
        if(tag.contains("connections")) {
            connections = new ArrayList<>();
            if(clientPacket) {
                ListTag connectionTags = tag.getList("connections", Tag.TAG_COMPOUND);
                for(int i = 0; i < connectionTags.size(); i++) {
                    CompoundTag connectionTag = connectionTags.getCompound(i);
                    BlockPos absolutePosition = NbtUtils.readBlockPos(connectionTag.getCompound("absolutePos"));
                    ResourceLocation connectionType = palette.decode(connectionTag.getInt("type"));
                    OverheadLineType overheadLineType = WireReg.get(connectionType);
                    if(overheadLineType == null) {
                        Kuayue.LOGGER.warn("Unknown connection type: " + connectionType);
                        continue;
                    }
                    Vector3f toPos = new Vector3f(connectionTag.getFloat("tX"), connectionTag.getFloat("tY"), connectionTag.getFloat("tZ"));
                    Connection connection = new Connection(
                            absolutePosition,
                            NbtUtils.readBlockPos(connectionTag.getCompound("absolutePos")).subtract(this.getBlockPos()),
                            overheadLineType,
                            connectionTag.getInt("index"),
                            connectionTag.getInt("targetIndex"),
                            toPos
                    );
                    connections.add(connection);
                }
            } else {
                ListTag connectionTags = tag.getList("connections", Tag.TAG_COMPOUND);
                for(int i = 0; i < connectionTags.size(); i++) {
                    CompoundTag connectionTag = connectionTags.getCompound(i);
                    BlockPos absolutePos = NbtUtils.readBlockPos(connectionTag.getCompound("absolutePos"));
                    BlockPos relativePos = NbtUtils.readBlockPos(connectionTag.getCompound("relativePos"));
                    ResourceLocation connectionType = palette.decode(connectionTag.getInt("type"));
                    OverheadLineType overheadLineType = WireReg.get(connectionType);
                    if(overheadLineType == null || !this.configuration.typePredictor().test(overheadLineType)) {
                        Kuayue.LOGGER.warn("OverheadLineSupportBlockEntity: {} connection type {} is not valid", this.getBlockPos(), connectionType);
                        continue;
                    }
                    Vector3f toPos = new Vector3f(connectionTag.getFloat("tX"), connectionTag.getFloat("tY"), connectionTag.getFloat("tZ"));
                    Connection connection = new Connection(
                            absolutePos,
                            relativePos,
                            overheadLineType,
                            connectionTag.getInt("index"),
                            connectionTag.getInt("targetIndex"),
                            toPos
                    );
                    connections.add(connection);
                }
            }
        }
        if(this.level != null) {
            this.connectionPoints = configuration.connectionPoints().get(this.level, this.getBlockPos(), this.getBlockState());
            onConnectionModification();
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);

        tag.putFloat("x_offset", x_offset);
        tag.putFloat("y_offset", y_offset);
        tag.putFloat("z_offset", z_offset);
        tag.putFloat("rotation", rotation);
        
        ResourcePattle palette = new ResourcePattle();
        ListTag connectionTags = new ListTag();
        for(int i = 0; i < connections.size(); i++) {
            Connection connection = connections.get(i);
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.put("absolutePos", NbtUtils.writeBlockPos(connection.absolutePos()));
            if(!clientPacket){
                connectionTag.put("relativePos", NbtUtils.writeBlockPos(connection.relativePos()));
            }
            connectionTag.putInt("type", palette.encode(WireReg.getName(connection.type())));
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
    public void invalidate() {
        super.invalidate();
        if(this.level == null || !this.level.isClientSide)
            return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()->()-> OverheadLineRendererBridge.unloadBlockEntity(this));
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(Envs.isDevEnvironment()){
            this.connectionPoints = configuration.connectionPoints().get(this.level, this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void onChunkUnloaded() {
        if(this.level == null || !this.level.isClientSide)
            return;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()->()-> OverheadLineRendererBridge.unloadBlockEntity(this));
    }

    public List<Connection> getConnections(){
        return connections;
    }

    public void onConnectionModification(){
        if(this.level == null) {
            return;
        }
        
        if(this.level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()->()-> {
                clearBlockEntityRenderCache();
                updateConnectionToPosition();
                OverheadLineRendererBridge.setBlockEntity(this, this.connections);
            });
            return;
        }
        
        if(!connections.isEmpty()) {
            Kuayue.OVERHEAD.savedData.getMigration().setConnectionNode(
                    level,
                    this.getBlockPos(),
                    ForgeRegistries.BLOCKS.getKey(this.getBlockState().getBlock()),
                    this.getConnections()
            );
        } else {
            Kuayue.OVERHEAD.savedData.getMigration().removeConnectionNode(
                    level,
                    this.getBlockPos(),
                    ForgeRegistries.BLOCKS.getKey(this.getBlockState().getBlock())
            );
        }
    }


    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-200000, -200000,-200000), getBlockPos().offset(200000,200000,200000));
    }

    public void onPlacement() {
        boolean updated = false;
        Iterator<Connection> connectionIterator = this.connections.iterator();
        ArrayList<Connection> updatedConnections = new ArrayList<>();
        while(connectionIterator.hasNext()) {
            Connection connection = connectionIterator.next();
            if(!connection.absolutePos.subtract(this.getBlockPos()).equals(connection.relativePos)) {
                updated = true;
                Pair<Boolean, Optional<Connection>> newConnection = getFreshConnection(connection);
                if(newConnection.getFirst()) {
                    connectionIterator.remove();
                } else continue;
                if(newConnection.getSecond().isPresent()){
                    updatedConnections.add(newConnection.getSecond().get());
                }
            }
        }
        if(updated) {
            this.connections.addAll(updatedConnections);
        }
        if(updated) {
            onConnectionModification();
        }
        this.notifyUpdate();
    }


    boolean shouldRecheckTargetPositions = false;

    public void checkTargetPosition(){

    }

    public Optional<String> checkCanAcceptNewConnection() {
        if(connections.size() >= configuration.maxConnections()) {
            return Optional.of("overhead_line_max_connections_reached");
        }
        return Optional.empty();
    }

    public float getXOffset() {
        return x_offset;
    }

    public float getYOffset() {
        return y_offset;
    }

    public float getZOffset() {
        return z_offset;
    }

    public float getManualRotation() {
        return rotation;
    }

    public void setXOffset(float x_offset) {
        this.x_offset = x_offset;
        this.notifyUpdate();
        onConnectionModification();
    }

    public void setYOffset(float y_offset) {
        this.y_offset = y_offset;
        this.notifyUpdate();
        onConnectionModification();
    }

    public void setZOffset(float z_offset) {
        this.z_offset = z_offset;
        this.notifyUpdate();
        onConnectionModification();
    }

    public void setManualRotation(float rotation) {
        this.rotation = rotation;
        this.notifyUpdate();
        onConnectionModification();
    }

    public void setTransformParameters(float x_offset, float y_offset, float z_offset, float rotation) {
        this.x_offset = x_offset;
        this.y_offset = y_offset;
        this.z_offset = z_offset;
        this.rotation = rotation;
        
        this.notifyUpdate();
        onConnectionModification();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.kuayue.overhead_line_support_adjust");
    }

    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new OverheadLineSupportAdjustMenu(pContainerId, pPlayerInventory, this);
    }

    @OnlyIn(Dist.CLIENT)
    private void clearBlockEntityRenderCache() {
        if (OverheadLineRendererBridge.REGISTERED.containsKey(this)) {
            List<Connection> registeredConnections = OverheadLineRendererBridge.REGISTERED.get(this);
            for (Connection connection : registeredConnections) {
                OverheadLineRendererSystem.removeOverheadLine(this, connection);
            }
        }

        OverheadSupportBlockRenderer.clearCacheForBlockEntity(this);

        for (Connection connection : this.connections) {
            if (level != null) {
                BlockEntity targetEntity = level.getBlockEntity(connection.absolutePos());
                if (targetEntity instanceof OverheadLineSupportBlockEntity targetSupport) {
                    OverheadSupportBlockRenderer.clearCacheForBlockEntity(targetSupport);
                }
            }
        }
    }
}
