package willow.train.kuayue.systems.train_extension.conductor.registry;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;

@FunctionalInterface
public interface ConductorCandidateMatcher {
    @Nullable
    ConductorProvider match(BlockState state);
}
