package net.invtweaks.mixin;

import net.minecraft.client.gui.screen.container.ContainerBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ContainerBase.class)
public interface ContainerBaseMixin {
    @Accessor("containerWidth")
    public int getContainerWidth();

    @Accessor("containerHeight")
    public int getContainerHeight();
}
