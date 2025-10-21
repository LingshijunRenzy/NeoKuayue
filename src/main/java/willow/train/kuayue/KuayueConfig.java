package willow.train.kuayue;

import kasuga.lib.core.config.SimpleConfig;

public class KuayueConfig {

    public static final SimpleConfig CONFIG = new SimpleConfig()
            .client("Kuayue Client")
            .boolConfig("RECEIVE_COLOR_SHARE",
                    "Should receive color requests.", true)
            .doubleConfig("OVERHEAD_LINE_END_WEIGHT_HEIGHT",
                    "The height of the end weight", 3.0)
            .rangedDoubleConfig("OVERHEAD_LINE_SAGGING_COEFFICIENT",
                    "The sagging coefficient of the overhead line", 300.0, 1.0, 100000.0)
            .rangedIntConfig("OVERHEAD_LINE_SUPPORT_RENDER_DISTANCE",
                    "This value controls how far you could see those overhead line supports.",
                    128, 32, 65535)
            .registerConfigs();

    public static void invoke(){}
}
