package willow.train.kuayue.mixins.mixin;

import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PortableFluidInterfaceBlockEntity.class)
public interface AccessorPortableFluidInterfaceBlockEntity {
    @Invoker("stopTransferring")
    void invokeStopTransferring();
}
