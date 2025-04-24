package willow.train.kuayue.systems.overhead_line.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import kasuga.lib.core.client.render.texture.Matrix;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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
        matrix.store(buffer.slice(needle * 16,16));
    }

    public Matrix4f getHandler(){
        return handler;
    }

    public void load(int needle){
        handler.load(buffer.slice(needle * 16, 16));
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
                matrix4f.load(buffer.slice(index * 16, 16));
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
        Pair<Matrix3f, Vector3f> affine = Transformation.toAffine(matrix4f);
        Vector3f position = affine.getSecond();
        AABB outBoundingBox = null;
        for(int i=0;i<=1;i++){
            for(int j=0;j<=1;j++){
                for(int k=0;k<=1;k++){
                    identify.set(i,j,k);
                    identify.transform(affine.getFirst());
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
        matrixHandle.load(buffer.slice(i * 16, 16));
        return matrixHandle;
    }
}
