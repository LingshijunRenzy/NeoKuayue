package willow.train.kuayue.systems.train_extension.conductor.providers;

import lombok.NonNull;
import willow.train.kuayue.initial.AllConductorTypes;
import willow.train.kuayue.systems.train_extension.conductor.Conductable;
import willow.train.kuayue.systems.train_extension.conductor.ConductorProvider;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

public class LinklessConductorProvider implements ConductorProvider {
    public static final LinklessConductorProvider INSTANCE = new LinklessConductorProvider();

    @Override
    public @NonNull ConductorType getType() {
        return AllConductorTypes.LINKLESS;
    }

    @Override
    public @NonNull Conductable modifyConductor(@NonNull Conductable rawConductor) {
        rawConductor.setOffset(0.5f);
        return rawConductor;
    }
}
