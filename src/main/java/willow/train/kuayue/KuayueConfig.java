package willow.train.kuayue;

import kasuga.lib.core.config.SimpleConfig;

public class KuayueConfig {

    public static final SimpleConfig CONFIG = new SimpleConfig()
            .client("Kuayue Client")
            .boolConfig("RECEIVE_COLOR_SHARE", "Should receive color requests.", true)
            .doubleConfig("OVERHEAD_LINE_END_WEIGHT_HEIGHT", "The height of the end weight", 3.0)
            .registerConfigs();

    public static void invoke(){}
}
