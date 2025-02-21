package willow.train.kuayue.block.panels.block_entity;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
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

import static willow.train.kuayue.block.panels.pantograph.SingleArmPantographBlock.ENABLED;

public class SingleArmPantographBlockEntity extends SmartBlockEntity implements IContraptionMovementBlockEntity {

    private boolean isRisen;
    private PantographProps pantographType;

    public SingleArmPantographBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SingleArmPantographBlockEntity(BlockPos pos, BlockState state) {
        this(AllBlocks.HXD3D_PANTOGRAPH_ENTITY.getType(), pos, state);
        this.isRisen = state.getValue(ENABLED);
        if (state.getBlock() instanceof SingleArmPantographBlock block) {
            this.pantographType = block.getPantographType();
        }
    }

    public boolean isRisen() {
        return isRisen;
    }

    public PantographProps getPantographType() {
        return pantographType;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void update(StructureTemplate.StructureBlockInfo info, Player player, BlockPos pos, AbstractContraptionEntity entity) {

    }

    @Override
    protected AABB createRenderBoundingBox() {
        return AABB.ofSize(Vec3.atCenterOf(this.getBlockPos()), 5, 5, 5);
    }
}
