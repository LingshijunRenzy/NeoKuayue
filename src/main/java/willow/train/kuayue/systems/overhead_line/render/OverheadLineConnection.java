package willow.train.kuayue.systems.overhead_line.render;

import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.world.phys.Vec3;

public class OverheadLineConnection {
    protected Pair<Vec3, Vec3> connectionPositions;

    OverheadLineConnection(Vec3 first, Vec3 last) {
        connectionPositions = Pair.of(first, last);
    }

    public Pair<Vec3, Vec3> getConnectionPositions() {
        return connectionPositions;
    }
}
