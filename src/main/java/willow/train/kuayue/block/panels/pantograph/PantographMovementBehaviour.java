package willow.train.kuayue.block.panels.pantograph;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.sk89q.jchronic.utils.Span;
import kasuga.lib.core.create.device.TrainDeviceLocation;
import kasuga.lib.core.create.device.TrainDeviceManager;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.ContraptionNbtUpdatePacket;
import willow.train.kuayue.systems.device.AllDeviceSystems;
import willow.train.kuayue.systems.device.driver.devices.power.PantographState;
import willow.train.kuayue.systems.device.driver.devices.power.PantographSystem;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;
import willow.train.kuayue.utils.client.DebugDrawUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PantographMovementBehaviour implements MovementBehaviour {
    @Override
    public void startMoving(MovementContext context) {}

    WeakHashMap<MovementContext, AtomicInteger> ticker = new WeakHashMap<>();

    @Override
    public void tick(MovementContext context) {
        if(context.world.isClientSide)
            return;

        if(ticker.computeIfAbsent(context, k -> new AtomicInteger(0)).incrementAndGet() <= 10)
            return;

        ticker.get(context).set(0);

        Pair<TrainDeviceManager, TrainDeviceLocation> locator = TrainDeviceManager.getManager(context);

        if(!(context.contraption instanceof CarriageContraption carriageContraption))
            return;

        if(locator == null || locator.getSecond() == null)
            return;

        BlockEntity blockEntity = context.contraption.presentBlockEntities.get(context.localPos);

        if(blockEntity == null && (context.state.getBlock() instanceof EntityBlock entityBlock)) {
            blockEntity = entityBlock.newBlockEntity(context.localPos, context.state);
            if(blockEntity == null)
                return;
            blockEntity.setLevel(context.world);
            blockEntity.load(context.blockEntityData);
        }

        if(
                !(blockEntity instanceof IPantographBlockEntity pantographBlockEntity) ||
                !(blockEntity instanceof SyncedBlockEntity syncedBlockEntity)
        ) return;

        PantographSystem pantographSystem = locator.getFirst().getOrCreateSystem(AllDeviceSystems.PANTOGRAPH);

        if(pantographSystem == null)
            return;

        PantographState state = pantographSystem.getOrRegisterPantographState(
                locator.getSecond(),
                carriageContraption.getAssemblyDirection() == context.state.getValue(BlockStateProperties.HORIZONTAL_FACING) ?
                        Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE
        );

        if(state.shouldUpdate(pantographBlockEntity.isRisen())) {
            pantographBlockEntity.setRisen(state.isRisen());
            CompoundTag data = new CompoundTag();
            syncedBlockEntity.writeClient(data);
            writeData(context.contraption.entity, context.localPos, data);
        }

        if(context.blockEntityData.get("overhead_line_support_cache") == null) {
            CompoundTag data = new CompoundTag();
            CurrOverheadLineCache cache = new CurrOverheadLineCache();
            cache.write(data);

            context.blockEntityData.put("overhead_line_support_cache", data);
        }

        if(isOverheadLineCacheNeedUpdate(context)) {
            updateOverheadLineCache(context);
        }

        CompoundTag cacheData = context.blockEntityData.getCompound("overhead_line_support_cache");
        CurrOverheadLineCache cache = new CurrOverheadLineCache();
        cache.read(cacheData);

        //render a red box to show the current support pos
        if(cache.hasCurrSupport()){
            DebugDrawUtil.addDebugBox("pantograph_support_curr",
                    cache.getCurrSupportPos(),
                    1.0f,
                    0.0f,
                    0.0f,
                    0.7f);
        }

        //render a green box to show the current link pos
        if(cache.hasCurrLink()){
            DebugDrawUtil.addDebugBox("pantograph_link_curr",
                    cache.getCurrLink(),
                    0.0f,
                    1.0f,
                    0.0f,
                    0.7f);
        }


        Kuayue.LOGGER.debug("Pantograph at {}", context.position);
        Kuayue.LOGGER.debug("Current Support: {}", cache.hasCurrSupport() ? cache.getCurrSupportPos().toShortString() : "null");
        Kuayue.LOGGER.debug("Current Point Pos: {}", cache.getCurrPointPos());
        Kuayue.LOGGER.debug("Current Link: {}", cache.hasCurrLink() ? cache.getCurrLink().toShortString() : "null");
        Kuayue.LOGGER.debug("Next Point Pos: {}", cache.getNextPointPos());
    }



    @Override
    public boolean renderAsNormalBlockEntity() {
        return true;
    }

    public void writeData(AbstractContraptionEntity contraptionEntity, BlockPos localPos, CompoundTag tag) {
        AllPackets.INTERACTION.getChannel().send(
                PacketDistributor.TRACKING_ENTITY.with(()->contraptionEntity),
                new ContraptionNbtUpdatePacket(contraptionEntity.getId(), localPos, tag)
        );
    }

    private boolean isOverheadLineCacheNeedUpdate(MovementContext context) {
        CompoundTag data = context.blockEntityData.getCompound("overhead_line_support_cache");
        CurrOverheadLineCache cache = new CurrOverheadLineCache();
        cache.read(data);

        if(!cache.hasCurrSupport()) {
            DebugDrawUtil.clearAllDebugElements();
            return true;
        }

        if(!cache.hasCurrLink()) {
            DebugDrawUtil.clearAllDebugElements();
            return true;
        }

        BlockPos nextSupportPos = cache.getCurrLink();
        BlockPos currentSupportPos = cache.getCurrSupportPos();
        // 方向突然间反过来的话也要更新
        if (Vec3.atCenterOf(nextSupportPos).subtract(Vec3.atCenterOf(currentSupportPos))
                .dot(context.motion.normalize()) < 0.0f) {
            DebugDrawUtil.clearAllDebugElements();
            return true;
        }
        Vec3 nextPointPos = cache.getNextPointPos();
        Vec3 supportVec = nextPointPos.subtract(cache.getCurrPointPos());
        Vec3 pantographVec = Vec3.atCenterOf(new Vec3i(context.position.x, context.position.y, context.position.z))
                .subtract(cache.getCurrPointPos());

        double res = supportVec.dot(pantographVec) / (supportVec.length() * supportVec.length());

        if(res > 1.0){
            DebugDrawUtil.clearAllDebugElements();
            cache.setCurrSupportPos(nextSupportPos);
            cache.setCurrPointPos(nextPointPos);
            cache.clearCurrLink();

            cache.write(data);
            context.blockEntityData.put("overhead_line_support_cache", data);

            return true;
        } else if (res < 0.0){
            DebugDrawUtil.clearAllDebugElements();
            cache.clearAll();

            cache.write(data);
            context.blockEntityData.put("overhead_line_support_cache", data);

            return true;
        }

        return false;
    }

    // 嗅探，找到最近的可用挂架
    private @Nullable BlockPos getClosedSupport(MovementContext context, Vec3 forwardVec, int searchRadius) {
        BlockPos pantographPos = new BlockPos(context.position.x, context.position.y, context.position.z);
        BlockPos minPos = pantographPos.offset(-searchRadius, -searchRadius, -searchRadius);
        BlockPos maxPos = pantographPos.offset(searchRadius, searchRadius, searchRadius);

        List<BlockPos> supportPosList = new ArrayList<>();
        BlockPos.betweenClosedStream(minPos, maxPos)
                .forEach(blockPos -> {
                    if(context.world.getBlockEntity(blockPos) instanceof OverheadLineSupportBlockEntity) {
                        supportPosList.add(blockPos.immutable());
                    }
                });

        if(supportPosList.isEmpty()) {
            return null;
        }

        double dist = Double.MAX_VALUE;
        BlockPos supportPos = null;

        // only leave the most close support
        for(BlockPos blockPos : supportPosList) {
            BlockEntity blockEntity = context.world.getBlockEntity(blockPos);
            if(!(blockEntity instanceof OverheadLineSupportBlockEntity)) {
                continue;
            }
            Vec3 supportVec = Vec3.atCenterOf(blockPos).subtract(
                    Vec3.atCenterOf(pantographPos)
            );
            double currentDist = supportVec.cross(forwardVec).length() / forwardVec.length();
            if(currentDist < dist) {
                dist = currentDist;
                supportPos = blockPos;
            }
        }

        return supportPos;
    }

    private void updateOverheadLineCache(MovementContext context) {
        CompoundTag data = context.blockEntityData.getCompound("overhead_line_support_cache");
        CurrOverheadLineCache cache = new CurrOverheadLineCache();
        cache.read(data);

        int searchRadius = 5;
        double threshold = Math.cos(Math.toRadians(10));
        Vec3 forwardVec = context.motion.normalize();

        if(forwardVec.equals(Vec3.ZERO)) {
            return;
        }

        if(!cache.hasCurrLink()) {
            if(!cache.hasCurrSupport()) {
                // 这里是嗅探逻辑

                @Nullable BlockPos supportPos = getClosedSupport(context, forwardVec, searchRadius);

                if (supportPos == null) {
                    return;
                }

                BlockEntity blockEntity = context.world.getBlockEntity(supportPos);
                if(!(blockEntity instanceof OverheadLineSupportBlockEntity supportBlockEntity)) {
                    return;
                }

                List<OverheadLineSupportBlockEntity.Connection> connectionList = new ArrayList<>(supportBlockEntity.getConnections());
                fineBestConnection(context, forwardVec, supportBlockEntity, connectionList);

                if(connectionList.isEmpty()) {
                    // if after filtering, no connection is suitable, skip
                    cache.clearAll();
                } else if(connectionList.size() == 1) {
                    // if after filtering, have only one connection, it's the best one
                    OverheadLineSupportBlockEntity.Connection connection = connectionList.get(0);

                    cache.setCurrSupportPos(supportPos);
                    cache.setCurrPointPos(supportBlockEntity.getConnectionPointByIndex(connection.connectionIndex()));
                    cache.setCurrLink(connection.absolutePos());

                    BlockEntity targetBlockEntity = context.world.getBlockEntity(connection.absolutePos());
                    if(!(targetBlockEntity instanceof OverheadLineSupportBlockEntity targetSupportBlockEntity)) {
                        cache.clearAll();
                        return;
                    }
                    cache.setNextPointPos(targetSupportBlockEntity.getConnectionPointByIndex(connection.targetIndex()));
                } else {
                    // have multiple suitable connections, just set curr support. leave next empty.
                    cache.setCurrSupportPos(supportPos);
                    cache.setCurrPointPos(supportBlockEntity.getConnectionPointByIndex(
                            connectionList.get(0).connectionIndex()
                    ));
                }
            } else {
                BlockPos currSupportPos = cache.getCurrSupportPos();
                BlockEntity blockEntity = context.world.getBlockEntity(currSupportPos);

                if(!(blockEntity instanceof OverheadLineSupportBlockEntity supportBlockEntity)) {
                    cache.clearAll();
                    return;
                }

                List< OverheadLineSupportBlockEntity.Connection> connectionList = new ArrayList<>(supportBlockEntity.getConnections());
                fineBestConnection(context, forwardVec, supportBlockEntity, connectionList);

                // all connections are out, re-select on next tick.
                if(connectionList.isEmpty()) {
                    cache.clearAll();
                    return;
                }

                if(connectionList.size() == 1) {
                    OverheadLineSupportBlockEntity.Connection connection = connectionList.get(0);

                    cache.setCurrLink(connection.absolutePos());
                    BlockEntity targetBlockEntity = context.world.getBlockEntity(connection.absolutePos());
                    if(!(targetBlockEntity instanceof OverheadLineSupportBlockEntity targetSupportBlockEntity)) {
                        cache.clearAll();
                        return;
                    }
                    cache.setNextPointPos(targetSupportBlockEntity.getConnectionPointByIndex(connection.targetIndex()));
                }
                // if size > 1, do nothing for next tick to filter again
                //DEBUG render: blue boxes are candidates
                else {
                    DebugDrawUtil.clearAllDebugElements();
                    for(OverheadLineSupportBlockEntity.Connection connection : connectionList) {
                        DebugDrawUtil.addDebugBox("pantograph_support_curr",
                                connection.absolutePos(),
                                0.0f,
                                0.0f,
                                1.0f,
                                0.7f);
                    }
                }
            }
            cache.write(data);
            context.blockEntityData.put("overhead_line_support_cache", data);
        } else {
            // 有link, 有support
            BlockPos currSupportPos = cache.getCurrSupportPos();
            BlockPos nextSupportPos = cache.getCurrLink();
            Vec3 currentSupportIndex = cache.getCurrPointPos();
            Vec3 nextSupportIndex = cache.getNextPointPos();

            Vec3 nextPointBlock = Vec3.atCenterOf(cache.getCurrLink());
            Vec3 thisPointBlock = Vec3.atCenterOf(cache.getCurrSupportPos());
            Vec3 linkVec = nextPointBlock.subtract(thisPointBlock);

            // 要把方向反过来
            if (linkVec.dot(forwardVec) < 0.0) {
                cache.setCurrLink(currSupportPos);
                cache.setCurrPointPos(nextSupportIndex);

                cache.setCurrSupportPos(nextSupportPos);
                cache.setNextPointPos(currentSupportIndex);
                cache.write(data);
                context.blockEntityData.put("overhead_line_support_cache", data);
            }
        }
    }

    // if list is not null, filter the most suitable connections
    // if list is null, find suitable connections from all connections
    // call this function in cycle until list size is 1 or 0
    private void fineBestConnection(
            MovementContext context,
            Vec3 forward,
            OverheadLineSupportBlockEntity entity,
            List<OverheadLineSupportBlockEntity.Connection> connectionList
    ) {
        double outerThreshold = Math.cos(Math.toRadians(45.0));
        List<OverheadLineSupportBlockEntity.Connection> connections = entity.getConnections();

        if(connections.isEmpty() || forward.equals(Vec3.ZERO)) {
            return;
        }

        if(connectionList.isEmpty()) {
            connectionList.addAll(connections);
        }

        List<OverheadLineSupportBlockEntity.Connection> toRemove = new ArrayList<>();
        for(OverheadLineSupportBlockEntity.Connection connection : connectionList) {
            BlockEntity targetEntity = context.world.getBlockEntity(connection.absolutePos());
            if(!(targetEntity instanceof OverheadLineSupportBlockEntity targetSupportEntity))
                continue;

            Vec3 connectionVec = targetSupportEntity
                    .getConnectionPointByIndex(connection.targetIndex())
                    .subtract(entity.getConnectionPointByIndex(connection.connectionIndex()));

            Vec3 connectionHoriz = new Vec3(connectionVec.x, 0, connectionVec.z).normalize();
            Vec3 forwardHoriz = new Vec3(forward.x, 0, forward.z).normalize();

            double angleCos = connectionHoriz.dot(forwardHoriz) /
                    (connectionHoriz.length() * forwardHoriz.length());

            if(angleCos < outerThreshold){
                toRemove.add(connection);
                continue;
            }

            Vec3 w = new Vec3(
                    context.position.x() - entity.getConnectionPointByIndex(connection.connectionIndex()).x(),
                    0,
                    context.position.z() - entity.getConnectionPointByIndex(connection.connectionIndex()).z()
            );

            double dist = connectionHoriz.cross(w).length() / connectionHoriz.length();
            if(dist > 1.0) {
                toRemove.add(connection);
            }
        }
        connectionList.removeAll(toRemove);
    }
}
