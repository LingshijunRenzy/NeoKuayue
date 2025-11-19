package willow.train.kuayue.block.panels.conductor;

import lombok.NonNull;
import net.minecraft.world.phys.Vec2;
import willow.train.kuayue.block.panels.FullShapeDirectionalBlock;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

public class SS8CowCatcherBlock extends FullShapeDirectionalBlock implements ConductorProvider {
    public SS8CowCatcherBlock(Properties pProperties, Vec2 beginPos, Vec2 endPos) {
        super(pProperties, beginPos, endPos);
    }

    public SS8CowCatcherBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ConductorType getType() {
        return ConductorType.DUMMY;
    }

    @Override
    public @NonNull Conductable modifyConductor(@NonNull Conductable rawConductor) {
        rawConductor.setOffset(1);
        return rawConductor;
    }
}
