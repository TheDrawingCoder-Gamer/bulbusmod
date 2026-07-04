package gay.menkissing.bulbus.screen

import gay.menkissing.bulbus.content.block.entity.StasisStorageBlockEntity
import net.minecraft.world.Container
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

trait CommonStasisStorageMenu extends AbstractContainerMenu:
  def boxStart: Int
  def boxEnd: Int

  def playerInv: Inventory
  def container: Container

  // todo, criteria

  override def stillValid(player: Player): Boolean =
    container.stillValid(player)
  
  override def quickMoveStack(player: Player, slotIndex: Int): ItemStack =
    // ported from neo scala 1.21 code that was
    // ported from java code that was ported from scala code that was ported from java code
    var transferredItemStack = ItemStack.EMPTY
    val slot = this.slots.get(slotIndex)
    if slot.hasItem then
      val slotStack = slot.getItem
      transferredItemStack = slotStack.copy()
      val boxStart = this.boxStart
      val boxEnd = this.boxEnd
      val invEnd = boxEnd + 36
      if slotIndex < boxEnd then
        if !super.moveItemStackTo(slotStack, boxEnd, invEnd, true) then
          return ItemStack.EMPTY
      else if !slotStack.isEmpty && StasisStorageBlockEntity.StorageTests.isAccepted(slotStack) && !super.moveItemStackTo(slotStack, boxStart, boxEnd, false) then
        return ItemStack.EMPTY
      
      if slotStack.isEmpty then
        slot.setByPlayer(ItemStack.EMPTY)
      else
        slot.setChanged()
      
      if slotStack.getCount == transferredItemStack.getCount then
        return ItemStack.EMPTY
      slot.onTake(player, slotStack)
    transferredItemStack


object CommonStasisStorageMenu:
  class BoxSlot(container: Container, idx: Int, x: Int, y: Int) extends Slot(container, idx, x, y):
    override def mayPlace(itemStack: ItemStack): Boolean = StasisStorageBlockEntity.StorageTests.isAccepted(itemStack)