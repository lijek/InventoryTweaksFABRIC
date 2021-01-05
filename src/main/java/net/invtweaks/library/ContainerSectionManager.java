package net.invtweaks.library;

import net.invtweaks.library.ContainerManager.ContainerSection;
import net.minecraft.client.Minecraft;
import net.minecraft.container.ContainerBase;
import net.minecraft.container.slot.Slot;
import net.minecraft.item.ItemInstance;

import java.util.List;
import java.util.concurrent.TimeoutException;


/**
 * Allows to perform various operations on a single section of
 * the inventory and/or containers. Works in both single and multiplayer.
 * 
 * @author Jimeo Wan
 *
 */
public class ContainerSectionManager {

    private ContainerManager containerMgr;
    private ContainerSection section;
    
    public ContainerSectionManager(Minecraft mc, ContainerSection section) throws Exception {
        this.containerMgr = new ContainerManager(mc);
        this.section = section;
        
        if (!containerMgr.hasSection(section)) {
            throw new Exception("Section not available");
        }
    }

    public boolean move(int srcIndex, int destIndex) throws TimeoutException {
        return containerMgr.move(section, srcIndex, section, destIndex);
    }

    public boolean moveSome(int srcIndex, int destIndex, int amount) throws TimeoutException {
        return containerMgr.moveSome(section, srcIndex, section, destIndex, amount);
    }

    public boolean drop(int srcIndex) throws TimeoutException {
        return containerMgr.drop(section, srcIndex);
    }
    
    public boolean dropSome(int srcIndex, int amount) throws TimeoutException {
        return containerMgr.dropSome(section, srcIndex, amount);
    }
    
    public void leftClick(int index) throws TimeoutException {
        containerMgr.leftClick(section, index);
    }

    public void rightClick(int index) throws TimeoutException {
        containerMgr.rightClick(section, index);
    }

    public void click(int index, boolean rightClick) throws TimeoutException {
        containerMgr.click(section, index, rightClick);
    }

    public List<Slot> getSlots() {
        return containerMgr.getSlots(section);
    }

    public int getSize() {
        return containerMgr.getSize(section);
    }

    public int getFirstEmptyIndex() {
        return containerMgr.getFirstEmptyIndex(section);
    }

    public boolean isSlotEmpty(int slot) {
        return containerMgr.isSlotEmpty(section, slot);
    }

    public Slot getSlot(int index) {
        return containerMgr.getSlot(section, index);
    }
    
    public int getSlotIndex(int slotNumber) {
        if (isSlotInSection(slotNumber)) {
            return containerMgr.getSlotIndex(slotNumber);
        }
        else {
            return -1;
        }
    }

    public boolean isSlotInSection(int slotNumber) {
        return containerMgr.getSlotSection(slotNumber) == section;
    }
    
    public ItemInstance getItemStack(int index) throws NullPointerException, IndexOutOfBoundsException {
        return containerMgr.getItemStack(section, index);
    }

    public ContainerBase getContainer() {
        return containerMgr.getContainer();
    }
    
}
