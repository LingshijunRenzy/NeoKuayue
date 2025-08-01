package willow.train.kuayue.systems.overhead_line.block.support;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.wire.AllWires;

import java.util.ArrayList;
import java.util.List;


public class OverheadLineEndWeightBlockEntity extends OverheadLineSupportBlockEntity {

    public enum RenderState {
        EMPTY,
        SINGLE,
        DUAL
    }

    protected int height = 0;

    protected RenderState renderState = RenderState.EMPTY;

    public OverheadLineEndWeightBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public OverheadLineEndWeightBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
        setLazyTickRate(20);
    }


    @Override
    public void lazyTick() {
        super.lazyTick();
        if(this.level == null || !this.level.isClientSide)
            return;
        refreshHeight(this.level);
    }


    public void refreshHeight(Level level) {
        ChunkAccess chunkAccess = level.getChunk(this.getBlockPos());
        BlockPos blockPos = this.getBlockPos().below();
        for(;blockPos.getY() > chunkAccess.getMinBuildHeight(); blockPos = blockPos.below()) {
            if(!level.getBlockState(blockPos).isAir()) {
                this.height = this.getBlockPos().getY() - blockPos.getY();
                break;
            }
        }
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void onConnectionModification() {
        if(connections.isEmpty())
            renderState = RenderState.EMPTY;
        else if(connections.parallelStream().anyMatch((connection)-> connection.type() == AllWires.OVERHEAD_LINE_WIRE.getWireType()))
            renderState = RenderState.DUAL;
        else
            renderState = RenderState.SINGLE;

        this.calculateDynamicRotation();

        updateRemoteConnections();

        super.onConnectionModification();

        this.notifyUpdate();
    }

    public RenderState getRenderState() {
        return renderState;
    }

    private void updateRemoteConnections() {
        if (level == null || connections.isEmpty()) {
            return;
        }

        List<Vec3> myConnectionPoints = this.getActualConnectionPoints();

        for (Connection connection : connections) {
            BlockPos remotePos = connection.absolutePos();
            int myConnectionIndex = connection.connectionIndex();

            if (myConnectionIndex < myConnectionPoints.size()) {
                Vec3 correctPoint = myConnectionPoints.get(myConnectionIndex);
                Vector3f correctVector = new Vector3f((float)correctPoint.x(), (float)correctPoint.y(), (float)correctPoint.z());
                var remoteBlockEntity = level.getBlockEntity(remotePos);
                if (remoteBlockEntity instanceof OverheadLineSupportBlockEntity remoteSupportEntity) {
                    boolean success = remoteSupportEntity.updateConnectionToPosition(this.getBlockPos(), correctVector);
                    if (success) {
                        remoteSupportEntity.refreshRenderingForConnection(this.getBlockPos());
                    }
                }
            }
        }
    }

    protected float dynamicRotationAngle = 0.0f;
    private boolean isGettingBasePoints = false;

    private void calculateDynamicRotation() {
        if(connections.isEmpty()){
            this.dynamicRotationAngle = 0.0f;
            return;
        }
        Connection connection = connections.get(0);

        Vec3 myPos = Vec3.atCenterOf(this.getBlockPos());
        Vec3 targetPos = getDynamicTargetPosition(connection);

        this.dynamicRotationAngle = calculateAngle(myPos, targetPos);
    }

    private Vec3 getDynamicTargetPosition(Connection connection) {
        BlockPos targetBlockPos = connection.absolutePos();
        BlockPos myBlockPos = this.getBlockPos();
        if (level != null) {
            var targetBlockEntity = level.getBlockEntity(targetBlockPos);

            if (targetBlockEntity instanceof OverheadLineSupportBlockEntity targetSupport) {
                return targetSupport.getConnectionPointByIndex(connection.targetIndex());
            }
        }

        Vector3f toPos = connection.toPosition();
        Vec3 relativePos = Vec3.atCenterOf(targetBlockPos.subtract(myBlockPos));
        return relativePos.add(toPos.x(), toPos.y(), toPos.z());
    }

    private float calculateAngle(Vec3 pos1, Vec3 pos2) {
        double dx = pos2.x() - pos1.x();
        double dz = pos2.z() - pos1.z();

        double horizontal_distance = Math.sqrt(dx * dx + dz * dz);

        if (horizontal_distance < 1e-6) {
            return 0.0f;
        }

        double angleRadians = Math.atan2(-dz, dx);
        double result = Math.toDegrees(angleRadians);
        if(result < 0) {
            result += 360;
        }

        result = result % 360;

        return (float) result;
    }

    public float getDynamicRotationAngle() {
        return dynamicRotationAngle;
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        float oldAngle = this.dynamicRotationAngle;

        if(tag.contains("dynamicRotationAngle")) {
            this.dynamicRotationAngle = tag.getFloat("dynamicRotationAngle");
        }else{
            if (!connections.isEmpty() && Math.abs(oldAngle) > 1e-6) {
            } else {
                if (connections.isEmpty()) {
                    this.dynamicRotationAngle = 0.0f;
                }
            }
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putFloat("dynamicRotationAngle", dynamicRotationAngle);
    }

    @Override
    public float getRotation() {
        if(connections.isEmpty() || isGettingBasePoints) {
            return super.getRotation();
        } else {
            return dynamicRotationAngle;
        }
    }

    @Override
    public List<Vec3> getActualConnectionPoints() {
        if(connections.isEmpty()) {
            return super.getActualConnectionPoints();
        }

        List<Vec3> basePoints = getBaseConnectionPointsWithoutDynamicRotation();

        if (Math.abs(dynamicRotationAngle) < 1e-6) {
            return basePoints;
        }

        List<Vec3> rotatedPoints = new ArrayList<>();
        double rotationRadians = Math.toRadians(dynamicRotationAngle);
        Vec3 rotationCenter = Vec3.atCenterOf(this.getBlockPos());

        for (Vec3 point : basePoints) {
            Vec3 relativePoint = point.subtract(rotationCenter);

            double cos = Math.cos(rotationRadians);
            double sin = Math.sin(rotationRadians);
            double newX = relativePoint.x() * cos + relativePoint.z() * sin;
            double newZ = -relativePoint.x() * sin + relativePoint.z() * cos;

            Vec3 rotatedPoint = new Vec3(newX, relativePoint.y(), newZ).add(rotationCenter);

            rotatedPoints.add(rotatedPoint);
        }
        return rotatedPoints;
    }

    private List<Vec3> getBaseConnectionPointsWithoutDynamicRotation() {
        List<Vec3> rawPoints = this.getConnectionPoints();
        BlockPos pPos = this.getBlockPos();
        List<Vec3> worldPoints = new ArrayList<>();

        for(Vec3 pos : rawPoints) {
            worldPoints.add(pos.add(pPos.getX(), pPos.getY(), pPos.getZ()).add(BASIC_OFFSET));
        }
        return worldPoints;
    }

    @Override
    public Vec3 getConnectionPointByIndex(int index) {
        List<Vec3> actualPoints = getActualConnectionPoints();
        return actualPoints.get(index);
    }
}
