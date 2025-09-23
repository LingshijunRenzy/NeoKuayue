package willow.train.kuayue.block.panels.carport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DF5ChimneyBlock extends DF11GChimneyBlock{
    public DF5ChimneyBlock(Properties properties, boolean isCarport) {
        super(properties, isCarport);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        dF5SpawnParticles(pState, pLevel, pPos);
    }

    private static void dF5SpawnParticles(BlockState pState, Level pLevel, BlockPos pPos) {
        RandomSource pRandom = pLevel.random;
        Direction direction = pState.getValue(FACING);

        if (pState.getValue(LIT)) {
            for (int i = 0; i < 2; i++) {
                pLevel.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        (double)pPos.getX() + 1.0D,
                        (double)pPos.getY() + 1.0D,
                        (double)pPos.getZ() + 1.0D,
                        (direction == Direction.EAST || direction == Direction.WEST) ?
                                (double)(0.05F + pRandom.nextFloat() / 10.0F) : 0.0F,
                        0.2F,
                        (direction == Direction.SOUTH || direction == Direction.NORTH) ?
                                (double)(0.05F + pRandom.nextFloat() / 10.0F) : 0.0F);
            }
        }
    }
}
