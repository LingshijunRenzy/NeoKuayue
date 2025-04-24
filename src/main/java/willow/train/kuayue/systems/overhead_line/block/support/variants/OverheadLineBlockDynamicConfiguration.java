package willow.train.kuayue.systems.overhead_line.block.support.variants;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.types.OverheadLineType;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record OverheadLineBlockDynamicConfiguration(
        List<Vec3> connectionPoints,
        Predicate<OverheadLineType> typePredictor,
        List<ResourceLocation> renderTypes
) {}
