package net.invtweaks.library;

import net.minecraft.client.ClientInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.client.gui.screen.container.ContainerBase;
import net.minecraft.client.gui.screen.container.Dispenser;
import net.minecraft.client.gui.screen.container.DoubleChest;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.container.slot.Slot;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.entity.player.PlayerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemInstance;

import java.io.File;
import java.util.List;

/**
 * Obfuscation layer, used to centralize most calls to Minecraft code.
 * Eases transitions when Minecraft then MCP are updated.
 * 
 * @author Jimeo Wan
 *
 */
public class Obfuscation {

    protected Minecraft mc;

    public Obfuscation(Minecraft mc) {
        this.mc = mc;
    }

    // Minecraft members

    protected void addChatMessage(String message) {
        if (mc.overlay != null) {
            mc.overlay.addChatMessage(message);
        }
    }

    protected boolean isMultiplayerWorld() {
        return mc.hasLevel();
    }

    protected PlayerBase getThePlayer() {
        return mc.player;
    }

    protected ClientInteractionManager getPlayerController() {
        return mc.interactionManager;
    }

    protected ScreenBase getCurrentScreen() {
        return mc.currentScreen;
    }

    // EntityPlayer members

    protected PlayerInventory getInventoryPlayer() {
        return getThePlayer().inventory;
    }

    protected ItemInstance getCurrentEquippedItem() {
        return getThePlayer().getHeldItem();
    }

    protected net.minecraft.container.ContainerBase getCraftingInventory() {
        return getThePlayer().container;
    }

    protected PlayerContainer getPlayerContainer() {
        return (PlayerContainer) getThePlayer().playerContainer; // MCP name: inventorySlots
    }

    // InventoryPlayer members

    protected ItemInstance[] getMainInventory() {
        return getInventoryPlayer().main;
    }

    protected void setMainInventory(ItemInstance[] value) {
        getInventoryPlayer().main = value;
    }

    protected void setHasInventoryChanged(boolean value) {
        getInventoryPlayer().dirty = value;
    }

    protected void setHoldStack(ItemInstance stack) {
        getInventoryPlayer().setCursorItem(stack); // MCP name: setItemStack
    }

    protected boolean hasInventoryChanged() {
        return getInventoryPlayer().dirty;
    }

    protected ItemInstance getHoldStack() {
        return getInventoryPlayer().getCursorItem(); // MCP name: getItemStack
    }

    protected ItemInstance getFocusedStack() {
        return getInventoryPlayer().getHeldItem(); // MCP name: getCurrentItem
    }

    protected int getFocusedSlot() {
        return getInventoryPlayer().selectedHotbarSlot; // MCP name: currentItem
    }

    // ItemStack members

    protected ItemInstance createItemStack(int id, int size, int damage) {
        return new ItemInstance(id, size, damage);
    }

    protected ItemInstance copy(ItemInstance itemStack) {
        return itemStack.copy();
    }

    protected int getItemDamage(ItemInstance itemStack) {
        return itemStack.getDamage();
    }

    protected int getMaxStackSize(ItemInstance itemStack) {
        return itemStack.method_709();
    }

    protected int getStackSize(ItemInstance itemStack) {
        return itemStack.count;
    }

    protected void setStackSize(ItemInstance itemStack, int value) {
        itemStack.count = value;
    }

    protected int getItemID(ItemInstance itemStack) {
        return itemStack.itemId;
    }

    protected boolean areItemStacksEqual(ItemInstance itemStack1, ItemInstance itemStack2) {
        return ItemInstance.method_703(itemStack1, itemStack2);
    }
    
    protected boolean areSameItemType(ItemInstance itemStack1, ItemInstance itemStack2) {
        return itemStack1.isEqualIgnoreFlags(itemStack2) ||
                (itemStack1.method_717() &&
                        getItemID(itemStack1) == getItemID(itemStack2));
    }

    // PlayerController members

    protected ItemInstance clickInventory(ClientInteractionManager playerController,
            int windowId, int slot, int clickButton, boolean shiftHold,
            PlayerBase entityPlayer) {
        return playerController.method_1708(windowId, slot, clickButton,
                shiftHold, entityPlayer); /* func_27174_a */
    }

    // Container members

    protected int getWindowId(net.minecraft.container.ContainerBase container) {
        return container.currentContainerId;
    }

    protected List<?> getSlots(net.minecraft.container.ContainerBase container) {
        return container.slots;
    }

    protected Slot getSlot(net.minecraft.container.ContainerBase container, int i) {
        return (Slot) getSlots(container).get(i);
    }

    protected ItemInstance getSlotStack(net.minecraft.container.ContainerBase container, int i) {
        Slot slot = (Slot) getSlots(container).get(i);
        return (slot == null) ? null : slot.getItem(); /* getStack */
    }

    protected void setSlotStack(net.minecraft.container.ContainerBase container, int i, ItemInstance stack) {
        container.method_2089(i, stack); /* putStackInSlot */
    }

    // GuiContainer members

    protected net.minecraft.container.ContainerBase getContainer(ContainerBase guiContainer) {
        return guiContainer.container;
    }

    // Other

    protected boolean isChestOrDispenser(ScreenBase guiScreen) {
        return ((guiScreen instanceof DoubleChest /* GuiChest */
                && !guiScreen.getClass().getSimpleName().equals("MLGuiChestBuilding")) // Millenaire mod
        || guiScreen instanceof Dispenser /* GuiDispenser */);
    }
    
    protected int getKeycode(KeyBinding keyBinding) {
        return keyBinding.key;
    }
    
    // Static access

    /**
     * Returns the Minecraft folder ensuring: - It is an absolute path - It ends
     * with a folder separator
     */
    public static String getMinecraftDir() {
        String absolutePath = Minecraft.getGameDirectory().getAbsolutePath();
        if (absolutePath.endsWith(".")) {
            return absolutePath.substring(0, absolutePath.length() - 1);
        }
        if (absolutePath.endsWith(File.separator)) {
            return absolutePath;
        } else {
            return absolutePath + File.separatorChar;
        }
    }
    
    public static ItemInstance getHoldStackStatic(Minecraft mc) {
        return new Obfuscation(mc).getHoldStack();
    }

    public static ScreenBase getCurrentScreenStatic(Minecraft mc) {
        return new Obfuscation(mc).getCurrentScreen();
    }

}