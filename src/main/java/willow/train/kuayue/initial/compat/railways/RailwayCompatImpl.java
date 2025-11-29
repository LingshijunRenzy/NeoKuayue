package willow.train.kuayue.initial.compat.railways;

import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.content.buffer.headstock.HeadstockBlock;
import com.railwayteam.railways.content.buffer.headstock.HeadstockStyle;
import com.railwayteam.railways.content.buffer.single_deco.LinkPinBlock;
import willow.train.kuayue.systems.train_extension.conductor.registry.ConductorCandidateRegistry;
import willow.train.kuayue.systems.train_extension.conductor.registry.SimpleConductorProvider;

public class RailwayCompatImpl implements RailwayCompat{
    public RailwayCompatImpl(){
    }
    public RailwayCompatImpl(RailwayCompatImpl r){}

    @Override
    public void registerConductors() {
        ConductorCandidateRegistry.registerBlock(LinkPinBlock.class, SimpleConductorProvider.INSTANCE);
        ConductorCandidateRegistry.registerBlockState(
                state -> {
                    return state.getBlock() instanceof HeadstockBlock
                            && !state.getValue(HeadstockBlock.STYLE).equals(HeadstockStyle.PLAIN)
                            && !state.getValue(HeadstockBlock.STYLE).equals(HeadstockStyle.BUFFER);
                },
                SimpleConductorProvider.INSTANCE
        );
        ConductorCandidateRegistry.registerBlockState(
                state -> {
                    return state.getBlock() instanceof CopycatHeadstockBlock
                            && !state.getValue(CopycatHeadstockBlock.STYLE).equals(HeadstockStyle.PLAIN)
                            && !state.getValue(CopycatHeadstockBlock.STYLE).equals(HeadstockStyle.BUFFER);
                },
                SimpleConductorProvider.INSTANCE
        );
    }
}
