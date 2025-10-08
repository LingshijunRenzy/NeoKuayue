package willow.train.kuayue.systems.overhead_line.block.line;

import net.minecraft.world.level.block.entity.BlockEntity;
import willow.train.kuayue.systems.overhead_line.block.support.OverheadLineSupportBlockEntity;

import java.util.HashMap;
import java.util.List;

public class OverheadLineRendererBridge {
    public static HashMap<BlockEntity, List<OverheadLineSupportBlockEntity.Connection>> REGISTERED = new HashMap();

    public static void setBlockEntity(OverheadLineSupportBlockEntity blockEntity, List<OverheadLineSupportBlockEntity.Connection> connections){
        unloadBlockEntity(blockEntity);

        if(REGISTERED.containsKey(blockEntity)){
            REGISTERED.get(blockEntity).forEach((c)->{
                OverheadLineRendererSystem.removeOverheadLine(blockEntity, c);
            });
        }

        connections.forEach((c)->{
            OverheadLineRendererSystem.registerOverheadLine(blockEntity, c);
        });

        REGISTERED.put(blockEntity, List.copyOf(connections));
    }

    public static void unloadBlockEntity(OverheadLineSupportBlockEntity blockEntity) {
        if(REGISTERED.containsKey(blockEntity)) {
            REGISTERED.get(blockEntity).forEach((c)->{
                OverheadLineRendererSystem.removeOverheadLine(blockEntity, c);
            });
            REGISTERED.remove(blockEntity);
        }
    }
}
