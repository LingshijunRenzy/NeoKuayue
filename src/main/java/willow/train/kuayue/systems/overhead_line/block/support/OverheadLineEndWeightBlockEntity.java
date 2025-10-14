package willow.train.kuayue.systems.overhead_line.block.support;

import org.joml.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import willow.train.kuayue.systems.overhead_line.wire.AllWires;


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

        if(this.preConnectionsSize == 0 && !connections.isEmpty()) {
            this.calculateDynamicRotation();
        }
        this.preConnectionsSize = connections.size();

        super.onConnectionModification();

        this.notifyUpdate();
    }

    private int preConnectionsSize = 0;

    public RenderState getRenderState() {
        return renderState;
    }

    private void calculateDynamicRotation() {
        if(connections.isEmpty()){
            return;
        }
        Connection connection = connections.get(0);

        Vec3 pPos = Vec3.atCenterOf(this.getBlockPos());
        PoseStack pose = new PoseStack();
        pose.translate(pPos.x(), pPos.y(), pPos.z());
        if (getBlockState().hasProperty(OverheadLineSupportBlock.FACING)) {
            Direction facing = getBlockState().getValue(OverheadLineSupportBlock.FACING);
            pose.mulPose(facing.getRotation());
            pose.mulPose(new Quaternion(-90, -90, 0, true));
        }

        pose.mulPose(Vector3f.YP.rotationDegrees(this.rotation * 1.03f));
        pose.translate(
                -this.x_offset * 1.3f,
                this.y_offset * 1.3f,
                -this.z_offset * 1.3f
                );
        Matrix4f m = pose.last().pose();
        Vector4f origin = new Vector4f(0f, 0f, 0f, 1f);
        origin.transform(m);
        Vec3 myPos = new Vec3(origin.x(), origin.y(), origin.z());

        Vec3 targetPos = getDynamicTargetPosition(connection);

        float absoluteAngle = calculateAngle(myPos, targetPos);

        float facingAngle = - this.getBlockState().getValue(OverheadLineSupportBlock.FACING).getOpposite().toYRot() - 90;
        this.rotation = absoluteAngle - facingAngle;

        while (this.rotation > 180) this.rotation -= 360;
        while (this.rotation <= -180) this.rotation += 360;
    }

    private Vec3 getDynamicTargetPosition(Connection connection) {
        BlockPos targetBlockPos = connection.absolutePos();
        BlockPos myBlockPos = this.getBlockPos();
        if (level != null) {
            var targetBlockEntity = level.getBlockEntity(targetBlockPos);

            if (targetBlockEntity instanceof OverheadLineSupportBlockEntity targetSupport) {
                return targetSupport.getConnectionPointByIndex(connection.targetIndex(), connection.type());
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

    @Override
    protected void onConnectionPositionUpdated(Connection updatedConnection, boolean fromExternal) {
        super.onConnectionPositionUpdated(updatedConnection, fromExternal);
        if(fromExternal && !this.connections.isEmpty()) {
            this.notifyUpdate();
            this.calculateDynamicRotation();
        }
    }
}
