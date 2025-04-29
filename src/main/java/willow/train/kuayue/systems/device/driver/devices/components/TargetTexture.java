package willow.train.kuayue.systems.device.driver.devices.components;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class TargetTexture extends AbstractTexture {

    public TargetTexture(TextureTarget textureTarget){
        this.id = textureTarget.getColorTextureId();
    }

    @Override
    public void releaseId() {}

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void load(ResourceManager pResourceManager) throws IOException {}
}
