package willow.train.kuayue.systems.device.graph;

import kasuga.lib.core.base.Saved;

public class KuaYueRailwayManager {
    public Saved<CRRailwayGraphData> savedData = new Saved<CRRailwayGraphData>(
            "kuayue_graph",
            CRRailwayGraphData::new,
            CRRailwayGraphData::load
    );

    public CRRailwayGraphData getSavedData() {
        return savedData.getData().orElseThrow();
    }
}
