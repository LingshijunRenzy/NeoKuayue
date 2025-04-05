package willow.train.kuayue.utils;

import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;

public record StationMixinCache(int index, AbstractBogeyBlock bogey, double bogeySpacing) {

    public static StationMixinCache instance;
}
