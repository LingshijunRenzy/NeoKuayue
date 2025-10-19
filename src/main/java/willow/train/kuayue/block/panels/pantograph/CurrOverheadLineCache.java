package willow.train.kuayue.block.panels.pantograph;

import kasuga.lib.core.base.NbtSerializable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class CurrOverheadLineCache implements NbtSerializable {
    private BlockPos currSupportPos;
    // it's difficult to store whole Connection data, use next support pos to check if there's a link
    private BlockPos currLink;

    private Vec3 currPointPos;
    private Vec3 nextPointPos;

    public CurrOverheadLineCache(
            BlockPos currSupportPos,
            BlockPos currLink,
            Vec3 currPointPos,
            Vec3 nextPointPos
    ) {
        this.currSupportPos = currSupportPos;
        this.currLink = currLink;
        this.currPointPos = currPointPos;
        this.nextPointPos = nextPointPos;
    }

    public CurrOverheadLineCache() {
        this.currSupportPos = BlockPos.ZERO;
        this.currLink = BlockPos.ZERO;
        this.currPointPos = Vec3.ZERO;
        this.nextPointPos = Vec3.ZERO;
    }

    @Override
    public void write(CompoundTag nbt) {
        nbt.put("currSupportPos", NbtUtils.writeBlockPos(currSupportPos));
        nbt.put("nextSupportPos", NbtUtils.writeBlockPos(currLink));

        CompoundTag currPointTag = new CompoundTag();
        currPointTag.putDouble("x", currPointPos.x);
        currPointTag.putDouble("y", currPointPos.y);
        currPointTag.putDouble("z", currPointPos.z);
        nbt.put("currPointPos", currPointTag);

        CompoundTag nextPointTag = new CompoundTag();
        nextPointTag.putDouble("x", nextPointPos.x);
        nextPointTag.putDouble("y", nextPointPos.y);
        nextPointTag.putDouble("z", nextPointPos.z);
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

        CompoundTag nextPointTag = nbt.getCompound("nextPointPos");
        this.nextPointPos = new Vec3(
                nextPointTag.getDouble("x"),
                nextPointTag.getDouble("y"),
                nextPointTag.getDouble("z")
        );
    }

    public boolean hasCurrSupport(){
        return !this.currSupportPos.equals(BlockPos.ZERO);
    }

    public boolean hasCurrLink(){
        return !this.currLink.equals(BlockPos.ZERO);
    }

    public void clearCurrLink(){
        this.currLink = BlockPos.ZERO;
        this.nextPointPos = Vec3.ZERO;
    }

    public void clearAll(){
        this.currSupportPos = BlockPos.ZERO;
        this.currLink = BlockPos.ZERO;
        this.currPointPos = Vec3.ZERO;
        this.nextPointPos = Vec3.ZERO;
    }
}