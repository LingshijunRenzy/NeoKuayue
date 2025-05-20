package willow.train.kuayue.block.panels.pantograph;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import kasuga.lib.core.create.device.TrainDeviceLocation;
import kasuga.lib.core.create.device.TrainDeviceManager;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.PacketDistributor;
import willow.train.kuayue.initial.AllPackets;
import willow.train.kuayue.network.s2c.ContraptionNbtUpdatePacket;
import willow.train.kuayue.systems.device.AllDeviceSystems;
import willow.train.kuayue.systems.device.driver.devices.power.PantographState;
import willow.train.kuayue.systems.device.driver.devices.power.PantographSystem;

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
                carriageContraption.getAssemblyDirection() == context.state.getValue(DirectionalBlock.FACING) ?
                        Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE
        );

        if(state.shouldUpdate(pantographBlockEntity.isRisen())) {
            pantographBlockEntity.setRisen(state.isRisen());
            CompoundTag data = new CompoundTag();
            syncedBlockEntity.writeClient(data);
            writeData(context.contraption.entity, context.localPos, data);
        }
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
}
