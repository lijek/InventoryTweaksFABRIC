package net.invtweaks.mixin;

import net.minecraft.client.gui.screen.ScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ScreenBase.class)
public interface ScreenBaseMixin {

    @Accessor("buttons")
    public List getButtons();
}
