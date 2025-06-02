package willow.train.kuayue.systems.overhead_line.block.decorating;

import kasuga.lib.registrations.common.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleOverheadLineDecoratingBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty GROUNDED = BooleanProperty.create("grounded");

    private final DecorationProperties decorationProperties;

    public SimpleOverheadLineDecoratingBlock(Properties pProperties, DecorationProperties decorationProperties) {
        super(pProperties.noOcclusion());
        this.decorationProperties = decorationProperties;
        this.registerDefaultState();
    }

    private void registerDefaultState() {
        BlockState state = this.defaultBlockState();
        if(decorationProperties.isDirectional()) {
            state = state.setValue(FACING, Direction.NORTH);
        }
        if(decorationProperties.hasGroundModel()) {

        }
        super.registerDefaultState(state);
        return;
    }

    public static DecorationProperties defaultProperties() {
        return new DecorationProperties();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext)
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING));
    }

    @Override
    public int getLightBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return false;
    }

    public static class Builder {
        public static BlockReg.BlockBuilder<SimpleOverheadLineDecoratingBlock> create(DecorationProperties properties) {
            return (p)->new SimpleOverheadLineDecoratingBlock(p, properties);
        }
    }

    public static class DecorationProperties {
        protected boolean $directional = false;
        protected List<BlockPos> $multiBlockPosition = null;
        protected boolean $hasGroundModel = false;

        public DecorationProperties directional() {
            this.$directional = true;
            return this;
        }

        public DecorationProperties multiBlocked(List<BlockPos> relativePositions) {
            this.$multiBlockPosition = relativePositions;
            return this;
        }

        public DecorationProperties groundModel() {
            this.$hasGroundModel = true;
            return this;
        }


        public boolean isDirectional(){
            return this.$directional;
        }

        public List<BlockPos> getMultiBlockPosition() {
            return this.$multiBlockPosition;
        }

        public boolean hasGroundModel() {
            return this.$hasGroundModel;
        }
    }
}
