package willow.train.kuayue.block.panels.block_entity;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.block.panels.pantograph.PantographProps;
import willow.train.kuayue.block.panels.pantograph.SingleArmPantographBlock;
import willow.train.kuayue.initial.AllBlocks;

import java.util.List;
import java.util.Map;

public class SingleArmPantographBlockEntity extends SmartBlockEntity implements IContraptionMovementBlockEntity {

    private boolean isRisen = true;
    private PantographProps pantographType;
    public double pullRodAngle = 170.0;
    private double transPosY = -0.5;

    public SingleArmPantographBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SingleArmPantographBlockEntity(BlockPos pos, BlockState state) {
        this(AllBlocks.HXD3D_PANTOGRAPH_ENTITY.getType(), pos, state);
        if (state.getBlock() instanceof SingleArmPantographBlock block) {
            this.pantographType = block.getPantographType();
        }
//        if (level == null)
//            return;
//        BlockState belowBlockstate = level.getBlockState(pos.below());
//        if (!belowBlockstate.getBlock().getDescriptionId().equals("block.kuayue.hxd3d_carport_center")) {
//            this.transPosY = 0;
//        } else {
//            this.transPosY = -0.5;
//        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putBoolean("open", isRisen);
        tag.putDouble("trans_y", transPosY);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tag.getBoolean("open");
        tag.getDouble("trans_y");
    }

    public void setRisen(boolean risen) {
        isRisen = risen;
    }

    public boolean isRisen() {
        return isRisen;
    }

    public PantographProps getPantographType() {
        return pantographType;
    }

    public Map<String, PartialModel> getPantographModel() {
        if (this.level == null)
            return null;
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if (!(state.getBlock() instanceof SingleArmPantographBlock block))
            return null;
        return block.getPantographModel();
    }

    public double getTransPosY() {
        return transPosY;
    }

    @Override
    public void tick() {
        if(level == null)
            return;
        BlockState state = level.getBlockState(this.getBlockPos());
        if (!(state.getBlock() instanceof SingleArmPantographBlock))
            return;
        this.isRisen = level.getBlockState(this.getBlockPos()).getValue(SingleArmPantographBlock.OPEN);
        BlockState belowBlockstate = level.getBlockState(this.getBlockPos().below());
        if (!belowBlockstate.getBlock().getDescriptionId().equals("block.kuayue.hxd3d_carport_center")) {
            this.transPosY = 0;
        } else {
            this.transPosY = -0.5;
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void update(StructureTemplate.StructureBlockInfo info, Player player, BlockPos pos, AbstractContraptionEntity entity) {
        this.isRisen = !isRisen;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return AABB.ofSize(Vec3.atCenterOf(this.getBlockPos()), 5, 5, 5);
    }
}
