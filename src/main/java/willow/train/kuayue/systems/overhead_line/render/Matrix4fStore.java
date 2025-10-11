package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import kasuga.lib.core.client.render.texture.Matrix;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix2d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
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

    /**
     * Theoretically it should be {@link Matrix4f#get(FloatBuffer)} here.
     * <br> But there may be some bugs in {@link org.joml.MemUtil.MemUtilUnsafe#put(Matrix4f, long)},
     * which would cause {@code EXCEPTION_ACCESS_VIOLATION}, this would lead to a JVM failure.
     * <br> SO, I write a pair of {@code compat methods} to fix this problem. they are just the
     * same logic of Minecraft used in {@code 1.19.2}.
     * See {@link Matrix4fStore#compatStore(Matrix4f, FloatBuffer)} and
     * {@link Matrix4fStore#compatLoad(Matrix4f, FloatBuffer)} below.
     * @author MegumiKasuga(Carole)
     * @param needle offset of the memory pointer.
     * @param matrix the matrix you want to write to this buffer.
     */
    public void write(int needle, Matrix4f matrix) {
        // matrix.get(needle * 16, buffer);
        // matrix.set(buffer.slice(needle * 16,16));
        // NOTICE: compat version of storage method is used.
        compatStore(matrix, buffer.slice(needle * 16, 16));
    }

    public Matrix4f getHandler(){
        return handler;
    }

    /**
     * Theoretically it should be {@link Matrix4f#set(FloatBuffer)} here.
     * <br> But there may be some bugs in {@link org.joml.MemUtil.MemUtilUnsafe#get(Matrix4f, long)},
     * which would cause {@code EXCEPTION_ACCESS_VIOLATION}, this would lead to a JVM failure.
     * <br> SO, I write a pair of {@code compat methods} to fix this problem. they are just the
     * same logic of Minecraft used in {@code 1.19.2}.
     * See {@link Matrix4fStore#compatStore(Matrix4f, FloatBuffer)} and
     * {@link Matrix4fStore#compatLoad(Matrix4f, FloatBuffer)}.
     * @author MegumiKasuga(Carole)
     * @param needle offset of the memory pointer.
     */
    public void load(int needle) {
        // handler.set(buffer.slice(needle * 16, 16));
        // NOTICE: compat version of loading method is used.
        compatLoad(handler, buffer.slice(needle * 16, 16));
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
                // NOTICE: compat version of loading method is used.
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
        // NOTICE: compat version of loading method is used.
        compatLoad(matrixHandle, buffer.slice(i * 16, 16));
        // matrixHandle.set(buffer.slice(i * 16, 16));
        return matrixHandle;
    }

    /**
     * The previous version of data storage from Minecraft {@code 1.19.2}.
     * <br> It could be only used for compat reason in {@link Matrix4fStore}.
     * @param matrix the matrix you want to store in.
     * @param buffer data in the input matrix would be filled into this buffer.
     * @author MegumiKasuga(Carole)
     */
    private static void compatStore(Matrix4f matrix, FloatBuffer buffer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buffer.put(bufferIndex(i, j), matrix.get(i, j));
            }
        }
    }

    /**
     * Utility method.
     * <br> It could be only used for compat reason in {@link Matrix4fStore}.
     * @param px row.
     * @param py col.
     * @author MegumiKasuga(Carole)
     */
    public static int bufferIndex(int px, int py) {
        return py * 4 + px;
    }

    /**
     * The previous version of data loading from Minecraft {@code 1.19.2}.
     * <br> It could be only used for compat reason in {@link Matrix4fStore}.
     * @param buffer the buffer you want to load from.
     * @param matrix data in the buffer would be filled into this matrix.
     * @author MegumiKasuga(Carole)
     */
    private static void compatLoad(Matrix4f matrix, FloatBuffer buffer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrix.set(i, j, buffer.get(bufferIndex(i, j)));
            }
        }
    }
}
