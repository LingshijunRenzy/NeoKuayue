package willow.train.kuayue.block.bogey.loco;


import com.simibubi.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import willow.train.kuayue.initial.create.AllLocoBogeys;

public class AndesiteLocoBogeyEntity extends AbstractBogeyBlockEntity {
    public AndesiteLocoBogeyEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AndesiteLocoBogeyEntity(BlockPos pos, BlockState state) {
        super(AllLocoBogeys.andesiteLocoBogeyEntity.getType(), pos, state);
    }

    @Override
    public BogeyStyle getDefaultStyle() {
        return AllLocoBogeys.andesiteLocoBogeyGroup.getStyle();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox();
    }
}
