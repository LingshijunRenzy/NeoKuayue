package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import kasuga.lib.core.client.render.texture.Matrix;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.util.Iterator;

public class Matrix4fStore implements Iterable<Matrix4f> {
    private final FloatBuffer buffer;
    private final int size;
    private final Matrix4f handler;

    public Matrix4fStore(FloatBuffer buffer, int size) {
        if(buffer.capacity() < 16 * size) {
            throw new IndexOutOfBoundsException(String.format("Invalid PointStack cache result: should the buffer capacity %d, %d got." , buffer.capacity(), size * 16));
        }
        this.buffer = buffer;
        this.size = size;
        this.handler = new Matrix4f();
    }

    public void write(int needle, Matrix4f matrix) {
        // matrix.get(needle * 16, buffer);
        // matrix.set(buffer.slice(needle * 16,16));
        compatStore(matrix, buffer.slice(needle * 16, 16));
    }

    private static void compatStore(Matrix4f matrix, FloatBuffer buffer) {
        buffer.put(bufferIndex(0, 0), matrix.m00());
        buffer.put(bufferIndex(0, 1), matrix.m01());
        buffer.put(bufferIndex(0, 2), matrix.m02());
        buffer.put(bufferIndex(0, 3), matrix.m03());
        buffer.put(bufferIndex(1, 0), matrix.m10());
        buffer.put(bufferIndex(1, 1), matrix.m11());
        buffer.put(bufferIndex(1, 2), matrix.m12());
        buffer.put(bufferIndex(1, 3), matrix.m13());
        buffer.put(bufferIndex(2, 0), matrix.m20());
        buffer.put(bufferIndex(2, 1), matrix.m21());
        buffer.put(bufferIndex(2, 2), matrix.m22());
        buffer.put(bufferIndex(2, 3), matrix.m23());
        buffer.put(bufferIndex(3, 0), matrix.m30());
        buffer.put(bufferIndex(3, 1), matrix.m31());
        buffer.put(bufferIndex(3, 2), matrix.m32());
        buffer.put(bufferIndex(3, 3), matrix.m33());
    }

    public static int bufferIndex(int px, int py) {
        return py * 4 + px;
    }

    public Matrix4f getHandler(){
        return handler;
    }

    public void load(int needle){
        // handler.set(buffer.slice(needle * 16, 16));
        compatLoad(handler, buffer.slice(needle * 16, 16));
    }

    private static void compatLoad(Matrix4f matrix, FloatBuffer buffer) {
        matrix.set(0, 0, buffer.get(bufferIndex(0, 0)));
        matrix.set(0, 1, buffer.get(bufferIndex(0, 1)));
        matrix.set(0, 2, buffer.get(bufferIndex(0, 2)));
        matrix.set(0, 3, buffer.get(bufferIndex(0, 3)));
        matrix.set(1, 0, buffer.get(bufferIndex(1, 0)));
        matrix.set(1, 1, buffer.get(bufferIndex(1, 1)));
        matrix.set(1, 2, buffer.get(bufferIndex(1, 2)));
        matrix.set(1, 3, buffer.get(bufferIndex(1, 3)));
        matrix.set(2, 0, buffer.get(bufferIndex(2, 0)));
        matrix.set(2, 1, buffer.get(bufferIndex(2, 1)));
        matrix.set(2, 2, buffer.get(bufferIndex(2, 2)));
        matrix.set(2, 3, buffer.get(bufferIndex(2, 3)));
        matrix.set(3, 0, buffer.get(bufferIndex(3, 0)));
        matrix.set(3, 1, buffer.get(bufferIndex(3, 1)));
        matrix.set(3, 2, buffer.get(bufferIndex(3, 2)));
        matrix.set(3, 3, buffer.get(bufferIndex(3, 3)));
    }

    @NotNull
    @Override
    public Iterator<Matrix4f> iterator() {
        return new Iterator<>() {
            Matrix4f matrix4f = new Matrix4f();
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < size;
            }
            @Override
            public Matrix4f next() {
                compatLoad(matrix4f, buffer.slice(index * 16, 16));
                // matrix4f.set(buffer.slice(index * 16, 16));
                index++;
                return matrix4f;
            }
        };
    }

    public int getSize() {
        return size;
    }

    public AABB getBoundingBox(){
        AABB aabb = null;
        Vector3f identify = new Vector3f(0,0,0);
        for (Matrix4f matrix4f : this) {
            AABB vertexAABB = getBoundingBoxOf(matrix4f, identify);
            if(aabb == null)
                aabb = vertexAABB;
            else
                aabb = aabb.minmax(vertexAABB);
        }
        return aabb;
    }

    public AABB[] getAllBoundingBox(){
        AABB[] aabbs = new AABB[size];
        Vector3f identify = new Vector3f(0,0,0);
        for (int i = 0; i < size; i++) {
            AABB vertexAABB = getBoundingBoxOf(this.getHandler(), identify);
            aabbs[i] = vertexAABB;
        }
        return aabbs;
    }

    public static AABB getBoundingBoxOf(Matrix4f matrix4f, Vector3f identify){
        Matrix3f rotation = new Matrix3f(matrix4f);
        rotation.scale(1.0F / matrix4f.m33());
        Transformation transformation = new Transformation(matrix4f);
        Vector3f position = transformation.getTranslation();
        AABB outBoundingBox = null;
        for(int i=0;i<=1;i++){
            for(int j=0;j<=1;j++){
                for(int k=0;k<=1;k++){
                    identify.set(i,j,k);
                    identify.mul(rotation);
                    AABB vertexAABB = new AABB(
                            position.x(),
                            position.y(),
                            position.z(),
                            position.x() + identify.x(),
                            position.y() + identify.y(),
                            position.z() + identify.z()
                    );

                    outBoundingBox = outBoundingBox == null ? vertexAABB : outBoundingBox.minmax(vertexAABB);
                }
            }
        }
        return outBoundingBox;
    }

    public Matrix4f get(Matrix4f matrixHandle, int i) {
        compatLoad(matrixHandle, buffer.slice(i * 16, 16));
        // matrixHandle.set(buffer.slice(i * 16, 16));
        return matrixHandle;
    }
}
