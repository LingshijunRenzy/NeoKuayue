package willow.train.kuayue.block.panels.carport;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;

public class DF11ChimneyBlock extends DF11GChimneyBlock {

    public DF11ChimneyBlock(Properties properties, boolean isCarport) {
        super(properties, isCarport);
        registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(true)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return super.onWrenched(state, context);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        return super.onSneakWrenched(state, context);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        super.animateTick(state, world, pos, rand);
    }

}

