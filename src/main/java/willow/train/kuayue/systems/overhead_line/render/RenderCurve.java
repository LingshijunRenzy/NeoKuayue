package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import kasuga.lib.core.util.data_type.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class RenderCurve implements Iterable<Pair<Matrix4f, Integer>> {
    private final Matrix4fStore matrix;

    private final int[] lightStore;

    public RenderCurve(int size) {
        this.matrix = new Matrix4fStore(FloatBuffer.allocate(size * 16), size);
        this.lightStore = new int[size];
    }

    public Iterator<Pair<Matrix4f, Integer>> iterator() {
        return new Iterator<>() {
            Matrix4f matrixHandle = new Matrix4f();
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < matrix.getSize();
            }

            @Override
            public Pair<Matrix4f, Integer> next() {
                return Pair.of(matrix.get(matrixHandle, i), lightStore[i++]);
            }
        };
    }

    public static Builder create(){
        return new Builder();
    }

    public Matrix4fStore getMatrix() {
        return matrix;
    }

    public static class Builder {
        private final PoseStack pose;
        private final ArrayList<Matrix4f> matrix4fs = new ArrayList<>();

        Builder(){
            pose = new PoseStack();
        }

        public PoseStack getPoseStack(){
            return pose;
        }

        public void store(){
            matrix4fs.add(pose.last().pose());
        }

        public RenderCurve build(Level level){
            RenderCurve curve = new RenderCurve(matrix4fs.size());
            for (int i = 0; i < matrix4fs.size(); i++) {
                curve.matrix.write(i, matrix4fs.get(i));
            }
            buildLightLevel(curve, level);
            return curve;
        }

        protected void buildLightLevel(RenderCurve curve, Level level){
            for (int i = 0; i < curve.matrix.getAllBoundingBox().length; i++) {
                curve.lightStore[i] = getLightAt(level, new BlockPos(curve.matrix.getAllBoundingBox()[i].getCenter()));
            }
        }

        protected int getLightAt(Level level, BlockPos blockPos) {
            return level.getLightEngine().getRawBrightness(blockPos,0);
        }
    }
}
