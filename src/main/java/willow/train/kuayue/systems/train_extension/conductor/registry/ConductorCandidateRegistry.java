package willow.train.kuayue.systems.train_extension.conductor.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import willow.train.kuayue.initial.AllCompats;
import willow.train.kuayue.initial.AllTags;
import willow.train.kuayue.initial.compat.railways.RailwayCompat;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ConductorCandidateRegistry {
    public static final List<ConductorCandidateMatcher> matchers = new ArrayList<>();

    public static void register(ConductorCandidateMatcher matcher) {
        matchers.add(matcher);
    }

    public static @Nullable ConductorProvider getProvider(BlockState state) {
        if(state.getBlock() instanceof ConductorProvider) {
            return (ConductorProvider) state.getBlock();
        }

        for(ConductorCandidateMatcher matcher : matchers) {
            ConductorProvider provider = matcher.match(state);
            if (provider != null) {
                return provider;
            }
        }
        return null;
    }

    public static void registerTag(TagKey<Block> tag, ConductorProvider provider) {
        register(state -> state.is(tag) ? provider : null);
    }

    public static void registerBlock(Block block, ConductorProvider provider) {
        register(state -> state.is(block) ? provider : null);
    }

    public static void registerBlock(Class<? extends Block> blockClass, ConductorProvider provider) {
        register(state -> blockClass.isInstance(state.getBlock()) ? provider : null);
    }

    public static void registerInterface(Class<?> interfaceClass, ConductorProvider provider) {
        register(state -> interfaceClass.isInstance(state.getBlock()) ? provider : null);
    }

    public static void registerBlockState(Predicate<BlockState> predicate, ConductorProvider provider) {
        register(state -> predicate.test(state) ? provider : null);
    }

    public static void invoke() {
        registerTag(AllTags.SIMPLE_CONDUCTOR.tag(), SimpleConductorProvider.INSTANCE);

        AllCompats.RAILWAYS.ifPresent(RailwayCompat::registerConductors);
    }
}
