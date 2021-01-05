package net.invtweaks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.modificationstation.stationapi.api.client.event.keyboard.KeyPressed;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegister;
import net.modificationstation.stationapi.api.common.mod.StationMod;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Environment(EnvType.CLIENT)
public class InvTweaksMod implements StationMod {

    public static InvTweaks instance;
    public static boolean hasInitialized = false;

    @Override
    public void init() {
        KeyBindingRegister.EVENT.register(new KeyBindingListener());
        KeyPressed.EVENT.register(new KeyPressedListener());
    }

    class KeyBindingListener implements KeyBindingRegister {

        @Override
        public void registerKeyBindings(List<KeyBinding> list) {
            list.add(Const.SORT_KEY_BINDING);
        }
    }

    class KeyPressedListener implements KeyPressed {

        @Override
        public void keyPressed() {
            if(Keyboard.getEventKey() == Const.SORT_KEY_BINDING.key){
                instance.onSortingKeyPressed();
            }
        }
    }
}
