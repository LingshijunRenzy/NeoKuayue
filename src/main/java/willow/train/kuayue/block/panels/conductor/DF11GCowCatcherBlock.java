package willow.train.kuayue.block.panels.conductor;

import lombok.NonNull;
import net.minecraft.world.phys.Vec2;
import willow.train.kuayue.block.panels.FullShapeDirectionalBlock;
import willow.train.kuayue.initial.AllConductorTypes;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

public class DF11GCowCatcherBlock extends FullShapeDirectionalBlock implements ConductorProvider {
    public DF11GCowCatcherBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos) {
        super(pProperties, beginPos, endPos);
    }

    public DF11GCowCatcherBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ConductorType getType() {
        return AllConductorTypes.JAN;
    }

    @Override
    public @NonNull Conductable modifyConductor(@NonNull Conductable rawConductor) {
        rawConductor.setOffset(1);
        return rawConductor;
    }
}
