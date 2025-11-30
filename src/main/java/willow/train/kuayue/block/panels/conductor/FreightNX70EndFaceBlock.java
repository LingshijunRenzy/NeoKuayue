package willow.train.kuayue.block.panels.conductor;

import lombok.NonNull;
import willow.train.kuayue.block.panels.end_face.FreightEndFaceBlock;
import willow.train.kuayue.initial.AllConductorTypes;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

public class FreightNX70EndFaceBlock extends FreightEndFaceBlock implements ConductorProvider {

    public FreightNX70EndFaceBlock(Properties properties, FreightType freightType) {
        super(properties, freightType);
    }

    @Override
    public @NonNull ConductorType getType() {
        return AllConductorTypes.DUMMY;
    }

    @Override
    public @NonNull Conductable modifyConductor(@NonNull Conductable rawConductor) {
        rawConductor.setOffset(1);
        return rawConductor;
    }
}
