package willow.train.kuayue.initial;

import net.minecraft.resources.ResourceLocation;
import willow.train.kuayue.Kuayue;
import willow.train.kuayue.systems.train_extension.conductor.Conductor;
import willow.train.kuayue.systems.train_extension.conductor.ConductorType;

public class AllConductorTypes {
    public static final ConductorType DUMMY = new ConductorType(
            new ResourceLocation("dummy"),
            Conductor::new,
            Conductor::new
    );

    // 销型车钩
    public static final ConductorType LINK = new ConductorType(
            new ResourceLocation("link"),
            Conductor::new,
            Conductor::new
    );

    // 无销车钩
    public static final ConductorType LINKLESS = new ConductorType(
            new ResourceLocation("linkless"),
            Conductor::new,
            Conductor::new
    );

    // 詹式车钩
    public static final ConductorType JAN = new ConductorType(
            new ResourceLocation("jan"),
            Conductor::new,
            Conductor::new
    );

    // 链式车钩
    public static final ConductorType SCREW_LINK = new ConductorType(
            new ResourceLocation("screw_link"),
            Conductor::new,
            Conductor::new
    );


    public static void invoke() {
        ConductorType.register(DUMMY);
        ConductorType.register(LINK);
        ConductorType.register(LINKLESS);
        ConductorType.register(JAN);
        ConductorType.register(SCREW_LINK);

        Kuayue.TRAIN_EXTENSION.register(DUMMY);
        Kuayue.TRAIN_EXTENSION.register(LINK);
        Kuayue.TRAIN_EXTENSION.register(LINKLESS);
        Kuayue.TRAIN_EXTENSION.register(JAN);
        Kuayue.TRAIN_EXTENSION.register(SCREW_LINK);
    }
}
