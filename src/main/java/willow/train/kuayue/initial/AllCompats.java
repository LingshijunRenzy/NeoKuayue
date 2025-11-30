package willow.train.kuayue.initial;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import willow.train.kuayue.initial.compat.railways.RailwayCompat;
import willow.train.kuayue.initial.compat.railways.RailwayCompatImpl;

import java.util.Optional;
import java.util.function.Supplier;

public class AllCompats {
    public static Optional<RailwayCompat> RAILWAYS =
            ((Optional<Supplier<Supplier<RailwayCompat>>>) (isRailwaysEnabled() ? Optional.of((Supplier<Supplier<RailwayCompat>>) () -> RailwayCompatImpl::new) : Optional.empty()))
                    .map(Supplier::get).map(Supplier::get);

    protected static boolean isRailwaysEnabled() {
        return FMLLoader.getLoadingModList().getModFileById("railways") != null;
    }

    public static void invoke() {}
}
