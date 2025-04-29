package willow.train.kuayue.systems.device.graph;

import kasuga.lib.core.base.Saved;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

public class KuaYueRailwayManager {
    public CRRailwayGraphData CLIENT = new CRRailwayGraphData();

    public CRRailwayGraphData SERVER = new CRRailwayGraphData();

    public CRRailwayGraphData sided(LevelAccessor accessor){
        if(accessor.isClientSide()){
            return CLIENT;
        }
        return SERVER;
    }
}
