package willow.train.kuayue.systems.train_extension.conductor;

import lombok.NonNull;

public interface ConductorProvider {

    @NonNull
    ConductorType getType();

    @NonNull
    Conductable modifyConductor(@NonNull Conductable rawConductor);
}
