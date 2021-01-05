package net.invtweaks;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemBase;

public class ExampleMod implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
		System.out.println("Look, merged client and server! : " + ItemBase.apple.getTranslatedName());
	}
}