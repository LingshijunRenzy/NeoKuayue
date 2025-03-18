package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class RenderCurve  {
    private final Matrix4fStore matrix;

    public RenderCurve(int size) {
        this.matrix = new Matrix4fStore(FloatBuffer.allocate(size * 16), size);
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

        public RenderCurve build(){
            RenderCurve curve = new RenderCurve(matrix4fs.size());
            for (int i = 0; i < matrix4fs.size(); i++) {
                curve.matrix.write(i, matrix4fs.get(i));
            }
            return curve;
        }
    }
}
