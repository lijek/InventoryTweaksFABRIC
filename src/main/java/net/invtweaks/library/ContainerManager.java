package net.invtweaks.library;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.container.*;
import net.minecraft.container.slot.Slot;
import net.minecraft.entity.player.PlayerContainer;
import net.minecraft.item.ItemInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Allows to perform various operations on the inventory
 * and/or containers. Works in both single and multiplayer.
 * 
 * @author Jimeo Wan
 *
 */
public class ContainerManager extends Obfuscation {
	
    // TODO: Throw errors when the container isn't available anymore

    public static final int DROP_SLOT = -999;
    public static final int INVENTORY_SIZE = 36;
    public static final int HOTBAR_SIZE = 9;
    public static final int ACTION_TIMEOUT = 500;
    public static final int POLLING_DELAY = 3;
    
    public enum ContainerSection{
        /** The player's inventory */ INVENTORY,
        /** The player's inventory (only the hotbar) */ INVENTORY_HOTBAR,
        /** The player's inventory (all except the hotbar) */ INVENTORY_NOT_HOTBAR,
        /** The chest or dispenser contents */ CHEST,
        /** The crafting input */ CRAFTING_IN,
        /** The crafting output */ CRAFTING_OUT,
        /** The armor slots */ ARMOR,
        /** The furnace input */ FURNACE_IN,
        /** The furnace output */ FURNACE_OUT,
        /** The furnace fuel */ FURNACE_FUEL,
        /** Any other type of slot. For unknown container types (such as
         * mod containers), only INVENTORY and OTHER sections are defined. */
        UNKNOWN
    }
    
    private ContainerBase container;
    private Map<ContainerSection, List<Slot>> slotRefs = new HashMap<ContainerSection, List<Slot>>();
    
    
    /**
     * Creates an container manager linked to the currently available container:
     * - If a container GUI is open, the manager gives access to this container contents.
     * - If no GUI is open, the manager works as if the player's inventory was open. 
     * @param mc Minecraft
     */
    @SuppressWarnings({"unchecked"})
    public ContainerManager(Minecraft mc) {
        super(mc);

        ScreenBase currentScreen = getCurrentScreen();
        if (currentScreen instanceof net.minecraft.client.gui.screen.container.ContainerBase) {
            this.container = getContainer((net.minecraft.client.gui.screen.container.ContainerBase) currentScreen);
        }
        else {
            this.container = getPlayerContainer();
        }
        
        List<Slot> slots = container.slots;
        int size = slots.size();
        boolean guiWithInventory = true;

        // Inventory: 4 crafting slots, then 4 armor slots, then inventory
        if (container instanceof PlayerContainer) {
            slotRefs.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            slotRefs.put(ContainerSection.CRAFTING_IN, slots.subList(1, 5));
            slotRefs.put(ContainerSection.ARMOR, slots.subList(5, 9));
        }
        
        // Chest/Dispenser
        else if ((container instanceof Chest)
                || (container instanceof Dispenser)) {
            slotRefs.put(ContainerSection.CHEST, slots.subList(0, size-INVENTORY_SIZE));
        }
        
        // Furnace
        else if ((container instanceof Furnace)) {
            slotRefs.put(ContainerSection.FURNACE_IN, slots.subList(0, 1));
            slotRefs.put(ContainerSection.FURNACE_FUEL, slots.subList(1, 2));
            slotRefs.put(ContainerSection.FURNACE_OUT, slots.subList(2, 3));
        }

        // Workbench
        else if ((container instanceof Crafting)) {
            slotRefs.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            slotRefs.put(ContainerSection.CRAFTING_IN, slots.subList(1, 10));
        }
        
        // Unknown
        else {
            if (size >= INVENTORY_SIZE) {
             // Assuming the container ends with the inventory, just like all vanilla containers.
                slotRefs.put(ContainerSection.UNKNOWN, slots.subList(0, size-INVENTORY_SIZE));
            }
            else {
                guiWithInventory = false;
                slotRefs.put(ContainerSection.UNKNOWN, slots.subList(0, size));
            }
        }

        if (guiWithInventory) {
            slotRefs.put(ContainerSection.INVENTORY, slots.subList(size-INVENTORY_SIZE, size));
            slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, slots.subList(size-INVENTORY_SIZE, size-HOTBAR_SIZE));
            slotRefs.put(ContainerSection.INVENTORY_HOTBAR, slots.subList(size-HOTBAR_SIZE, size));
        }
        
    }
    
    /**
     * Moves a stack from source to destination, adapting the behavior 
     * according to the context:
     * - If destination is empty, the source stack is moved.
     * - If the items can be merged, as much items are possible are put
     *   in the destination, and the eventual remains go back to the source.
     * - If the items cannot be merged, they are swapped.
     * @param srcSection The source section
     * @param srcIndex The destination slot
     * @param destSection The destination section
     * @param destIndex The destination slot
     * @return false if the source slot is empty or the player is
     * holding an item that couln't be put down.
     * @throws TimeoutException 
     */
	public boolean move(ContainerSection srcSection, int srcIndex,
            ContainerSection destSection, int destIndex) throws TimeoutException {
	    
	    ItemInstance srcStack = getItemStack(srcSection, srcIndex);
      ItemInstance destStack = getItemStack(destSection, destIndex);
	    
        if (srcStack == null) {
            return false;
        }
        else if (srcSection == destSection && srcIndex == destIndex) {
            return true;
        }

        // Put hold item down
        if (getHoldStack() != null) {
            int firstEmptyIndex = getFirstEmptyIndex(ContainerSection.INVENTORY);
            if (firstEmptyIndex != -1) {
                leftClick(ContainerSection.INVENTORY, firstEmptyIndex);
            }
            else {
                return false;
            }
        }
        
        boolean destinationEmpty = getItemStack(destSection, destIndex) == null;

        // Use intermediate slot if we have to swap tools, maps, etc.
        if (destStack != null
                && getItemID(srcStack) == getItemID(destStack)
                && srcStack.method_709() == 1) {
            int intermediateSlot = getFirstEmptyUsableSlotNumber();
            ContainerSection intermediateSection = getSlotSection(intermediateSlot);
            int intermediateIndex = getSlotIndex(intermediateSlot);
            if (intermediateIndex != -1) {
                // Step 1/3: Dest > Int
                leftClick(destSection, destIndex);
                leftClick(intermediateSection, intermediateIndex);
                // Step 2/3: Src > Dest
                leftClick(srcSection, srcIndex);
                leftClick(destSection, destIndex);
                // Step 3/3: Int > Src
                leftClick(intermediateSection, intermediateIndex);
                leftClick(srcSection, srcIndex);
            }
            else {
                return false;
            }
        }
        
        // Normal move
        else {
            leftClick(srcSection, srcIndex);
            leftClick(destSection, destIndex);
            if (!destinationEmpty) {
                leftClick(srcSection, srcIndex);
            }
        }
        
      
        
        return true;
    }
	    
	/**
     * Moves some items from source to destination.
	 * @param srcSection The source section
	 * @param srcIndex The destination slot
     * @param destSection The destination section
     * @param destIndex The destination slot
	 * @param amount The amount of items to move. If <= 0, does nothing.
	 * If > to the source stack size, moves as much as possible from the stack size.
	 * If not all can be moved to the destination, only moves as much as possible.
	 * @return false if the destination slot is already occupied
	 * by a different item (meaning items cannot be moved to destination).
	 * @throws TimeoutException 
	 */
	public boolean moveSome(ContainerSection srcSection, int srcIndex,
	        ContainerSection destSection, int destIndex,
	        int amount) throws TimeoutException {

        ItemInstance source = getItemStack(srcSection, srcIndex);
	    if (source == null || srcSection == destSection && srcIndex == destIndex) {
            return true;
        }

      ItemInstance destination = getItemStack(srcSection, srcIndex);
        int sourceSize = getStackSize(source);
        int movedAmount = Math.min(amount, sourceSize);
	    
	    if (source != null && (destination == null
	            || source.isEqualIgnoreFlags(destination))) {

	        leftClick(srcSection, srcIndex);
	        for (int i = 0; i < movedAmount; i++) {
	            rightClick(destSection, destIndex);
	        }
	        if (movedAmount < sourceSize) {
	            leftClick(srcSection, srcIndex);
	        }
	        return true;
	    }
	    else {
	        return false;
	    }
	    
	}

    public boolean drop(ContainerSection srcSection, int srcIndex) throws TimeoutException {
        return move(srcSection, srcIndex, null, DROP_SLOT);
    }
    
    public boolean dropSome(ContainerSection srcSection, int srcIndex, int amount) throws TimeoutException {
        return moveSome(srcSection, srcIndex, null, DROP_SLOT, amount);
    }
            
	public void leftClick(ContainerSection section, int index) throws TimeoutException {
        click(section, index, false);
    }

    public void rightClick(ContainerSection section, int index) throws TimeoutException {
        click(section, index, true);
    }

    public void click(ContainerSection section, int index, boolean rightClick) throws TimeoutException {
        
        int slot = indexToSlot(section, index);
       // int timeSpentWaiting = 0;
        
        if (slot != -1) {
            
            /* boolean uselessClick = false;
            ItemStack stackInSlot = null;
            if (isMultiplayerWorld()) {
                // After clicking, we'll need to wait for server answer before continuing.
                // We'll do this by listening to any change in the slot, but this implies we
                // check first if the click will indeed produce a change.
                stackInSlot = (getItemStack(section, index) != null)
                        ? copy(getItemStack(section, index)) : null;
                ItemStack stackInHand = getHoldStack();
            
                // Useless if empty stacks
                if (stackInHand == null && stackInSlot == null)
                    uselessClick = true;
                // Useless if destination stack is full
                else if (stackInHand != null && stackInSlot != null
                        && stackInHand.isItemEqual(stackInSlot)
                        && getStackSize(stackInSlot) == getMaxStackSize(stackInSlot)) {
                    uselessClick = true;
                }
            }*/
        
            // Click!
            clickInventory(getPlayerController(),
                    getWindowId(container), // Select container
                    slot, // Targeted slot
                    (rightClick) ? 1 : 0, // Click #
                    false, // Shift not held
                    getThePlayer());
        
            // Wait for inventory update
            /*if (isMultiplayerWorld()) {
                if (!uselessClick) {
                    int pollingTime = 0;
                    // Note: Polling doesn't work for crafting output, if the same recipe
                    // can still be used.
                    while (areItemStacksEqual(getItemStack(section, index), stackInSlot)
                            && pollingTime < ACTION_TIMEOUT
                            && section != ContainerSection.CRAFTING_OUT) { 
                        try {
                            Thread.sleep(POLLING_DELAY);
                        } catch (InterruptedException e) {
                            // Do nothing
                        }
                        pollingTime += POLLING_DELAY;
                    }
                    if (pollingTime >= ACTION_TIMEOUT) {
                        log.warning("Click timeout");
                    }
                    timeSpentWaiting += pollingTime;
                }
            }*/
        }
    }

    public boolean hasSection(ContainerSection section) {
        return slotRefs.containsKey(section);
    }

    public List<Slot> getSlots(ContainerSection section) {
        return slotRefs.get(section); 
    }

    /**
     * @return The size of the whole container
     */
    public int getSize() {
        int result = 0;
        for (List<Slot> slots : slotRefs.values()) {
            result += slots.size();
        }
        return result;
    }
    
    /**
     * Returns the size of a section of the container.
     * @param section
     * @return The size, or 0 if there is no such section.
     */
    public int getSize(ContainerSection section) {
        if (hasSection(section)) {
            return slotRefs.get(section).size();  
        }
        else {
            return 0;
        }
    }

    /**
     * 
     * @param section
     * @return -1 if no slot is free
     */
    public int getFirstEmptyIndex(ContainerSection section) {
        int i = 0;
        for (Slot slot : slotRefs.get(section)) { 
            if (!slot.hasItem()) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * @param slot
     * @return true if the specified slot exists and is empty, false otherwise.
     */
    public boolean isSlotEmpty(ContainerSection section, int slot) {
        if (hasSection(section)) {
            return getItemStack(section, slot) == null;
        }
        else {
            return false;
        }
    }

    public Slot getSlot(ContainerSection section, int index) {
        List<Slot> slots = slotRefs.get(section);
        if (slots != null) {
            return slots.get(index);
        } else {
            return null;
        }
    }
    
    public int getSlotIndex(int slotNumber) {
        // TODO Caching with getSlotSection
        for (ContainerSection section : slotRefs.keySet()) {
            if (section != ContainerSection.INVENTORY) {
                int i = 0;
                for (Slot slot : slotRefs.get(section)) {
                    if (slot.id == slotNumber) {
                        return i;
                    }
                    i++;
                }
            }
        }
        return -1;
    }
    
    /**
     * Note: Prefers INVENTORY_HOTBAR/NOT_HOTBAR instead of INVENTORY.
     * @param slotNumber
     * @return null if the slot number is invalid.
     */
    public ContainerSection getSlotSection(int slotNumber) {
        // TODO Caching with getSlotIndex
        for (ContainerSection section : slotRefs.keySet()) {
            if (section != ContainerSection.INVENTORY) {
                for (Slot slot : slotRefs.get(section)) {
                    if (slot.id == slotNumber) {
                        return section;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns an ItemStack from the wanted section and slot.
     * @param index
     * @param section
     * @return An ItemStack or null.
     */
    public ItemInstance getItemStack(ContainerSection section, int index)
            throws NullPointerException, IndexOutOfBoundsException {
        int slot = indexToSlot(section, index);
        if (slot >= 0 && slot < getSlots(container).size()) {
            return getSlotStack(container, slot);
        } else {
            return null;
        }
    }

    public ContainerBase getContainer() {
        return container;
    }

    private int getFirstEmptyUsableSlotNumber() {
        for (ContainerSection section : slotRefs.keySet()) {
            for (Slot slot : slotRefs.get(section)) {
                // Use only standard slot (to make sure
                // we can freely put and remove items there)
                if (slot.getClass().equals(Slot.class)
                        && !slot.hasItem()) {
                    return slot.id;
                }
            }
        }
        return -1;
    }
    
    /**
     * Converts section/index values to slot ID.
     * @param section
     * @param index
     * @return -1 if not found
     */
    private int indexToSlot(ContainerSection section, int index) {
        if (index == DROP_SLOT) {
            return DROP_SLOT;
        }
        if (hasSection(section)) {
            Slot slot = slotRefs.get(section).get(index);
            if (slot != null) {
                return slot.id;
            }
            else {
                return -1;
            }
        }
        else {
            return -1;
        }
    }
    
}
