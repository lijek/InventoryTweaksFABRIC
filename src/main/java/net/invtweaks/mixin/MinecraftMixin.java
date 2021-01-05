package net.invtweaks.mixin;

import net.invtweaks.InvTweaks;
import net.invtweaks.InvTweaksMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow private static Minecraft instance;

    @Inject(at=@At("RETURN"), method="tick")
    public void tick(CallbackInfo ci){
        if(!InvTweaksMod.hasInitialized){
            InvTweaksMod.instance = new InvTweaks(instance);
            InvTweaksMod.hasInitialized = true;
        }
        if(instance.level != null){
            InvTweaksMod.instance.onTickInGame();
        }
        if(instance.currentScreen != null){
            InvTweaksMod.instance.onTickInGUI(instance.currentScreen);
        }
    }
}
