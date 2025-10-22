package willow.train.kuayue.block.panels.pantograph;

import kasuga.lib.core.base.NbtSerializable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Objects;

@Getter
@Setter
public class CurrOverheadLineCache implements NbtSerializable {
    private BlockPos currSupportPos;
    // it's difficult to store whole Connection data, use next support pos to check if there's a link
    private BlockPos currLink;

    private Vec3 currPointPos;
    private Vec3 nextPointPos;

    private int currentPoint, nextPoint;

    private int cacheVectorIndex;
    private double cacheProgress;
    private double cacheHeight;
    private double cacheTotalHeight;
    private ArrayList<Vec2> cachedPoints;
    private ArrayList<Vec2> revertCachedPoints;


    public CurrOverheadLineCache(
            BlockPos currSupportPos,
            BlockPos currLink,
            Vec3 currPointPos,
            Vec3 nextPointPos,
            int currentPoint,
            int nextPoint
    ) {
        this.currSupportPos = currSupportPos;
        this.currLink = currLink;
        this.currPointPos = currPointPos;
        this.nextPointPos = nextPointPos;
        this.currentPoint = currentPoint;
        this.nextPoint = nextPoint;

        clearCache();
    }

    public CurrOverheadLineCache() {
        this.currSupportPos = BlockPos.ZERO;
        this.currLink = BlockPos.ZERO;
        this.currPointPos = Vec3.ZERO;
        this.nextPointPos = Vec3.ZERO;
        this.currentPoint = -1;
        this.nextPoint = -1;

        clearCache();
    }

    @Override
    public void write(CompoundTag nbt) {
        nbt.put("currSupportPos", NbtUtils.writeBlockPos(currSupportPos));
        nbt.put("nextSupportPos", NbtUtils.writeBlockPos(currLink));

        CompoundTag currPointTag = new CompoundTag();
        currPointTag.putDouble("x", currPointPos.x);
        currPointTag.putDouble("y", currPointPos.y);
        currPointTag.putDouble("z", currPointPos.z);
        currPointTag.putInt("currentPoint", currentPoint);
        nbt.put("currPointPos", currPointTag);

        CompoundTag nextPointTag = new CompoundTag();
        nextPointTag.putDouble("x", nextPointPos.x);
        nextPointTag.putDouble("y", nextPointPos.y);
        nextPointTag.putDouble("z", nextPointPos.z);
        nextPointTag.putInt("nextPoint", nextPoint);
        nbt.put("nextPointPos", nextPointTag);

    }

    @Override
    public void read(CompoundTag nbt){
        this.currSupportPos = NbtUtils.readBlockPos(nbt.getCompound("currSupportPos"));
        this.currLink = NbtUtils.readBlockPos(nbt.getCompound("nextSupportPos"));

        CompoundTag currPointTag = nbt.getCompound("currPointPos");
        this.currPointPos = new Vec3(
                currPointTag.getDouble("x"),
                currPointTag.getDouble("y"),
                currPointTag.getDouble("z")
        );
        this.currentPoint = currPointTag.getInt("currentPoint");

        CompoundTag nextPointTag = nbt.getCompound("nextPointPos");
        this.nextPointPos = new Vec3(
                nextPointTag.getDouble("x"),
                nextPointTag.getDouble("y"),
                nextPointTag.getDouble("z")
        );
        this.nextPoint = nextPointTag.getInt("nextPoint");
    }

    public boolean hasCurrSupport() {
        return this.currentPoint > -1;
    }

    public boolean hasCurrLink() {
        return this.nextPoint > -1;
    }

    public void clearCurrLink() {
        this.currLink = BlockPos.ZERO;
        this.nextPointPos = Vec3.ZERO;
        this.nextPoint = -1;

        clearCache();
    }

    public void clearAll(){
        this.currSupportPos = BlockPos.ZERO;
        this.currLink = BlockPos.ZERO;
        this.currPointPos = Vec3.ZERO;
        this.nextPointPos = Vec3.ZERO;
        this.currentPoint = -1;
        this.nextPoint = -1;

        clearCache();
    }

    public void clearCache() {
        this.cacheVectorIndex = -1;
        this.cacheProgress = 0;
        this.cacheHeight = 0;
        this.cacheTotalHeight = 0;
        this.cachedPoints = null;
        this.revertCachedPoints = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CurrOverheadLineCache c)) return false;
        return Objects.equals(currSupportPos, c.currSupportPos) &&
                Objects.equals(currLink, c.currLink) &&
                Objects.equals(currPointPos, c.currPointPos) &&
                Objects.equals(nextPointPos, c.nextPointPos) &&
                Objects.equals(currentPoint, c.currentPoint) &&
                Objects.equals(nextPoint, c.nextPoint);
    }
}