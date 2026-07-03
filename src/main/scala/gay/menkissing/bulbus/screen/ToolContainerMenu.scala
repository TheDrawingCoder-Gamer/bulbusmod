package gay.menkissing.bulbus.screen

import gay.menkissing.bulbus.inventory.ItemBackedInventory
import gay.menkissing.bulbus.registries.{BulbusScreens, BulbusTags}
import gay.menkissing.bulbus.screen.slots.LockedSlot
import net.minecraft.world.{Container, InteractionHand, SimpleContainer}
import net.minecraft.world.inventory.Slot
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class ToolContainerMenu(windowId: Int, playerInv: Inventory, val box: ItemStack) extends AbstractContainerMenu(BulbusScreens.toolContainer, windowId):
  locally:
    val boxInv =
      if !playerInv.player.level().isClientSide then
        new ItemBackedInventory(box, ToolContainerMenu.containerSize)
      else
        new SimpleContainer(ToolContainerMenu.containerSize)
    
    boxInv.startOpen(playerInv.player)
    
    val k = (ToolContainerMenu.rows - 4) * 18
    for
      i <- 0 until ToolContainerMenu.rows
      j <- 0 until ToolContainerMenu.columns
    do
      addSlot(new ToolContainerMenu.ToolSlot(boxInv, j + i * ToolContainerMenu.columns, 8 + j * 18, 18 + i * 18))
      
    for
      i <- 0 until ScreenCommon.playerRows
      j <- 0 until ScreenCommon.playerColumns
    do
      addSlot(new Slot(playerInv, j + i * ScreenCommon.playerColumns + 9, 8 + j * 18, 103 + i * 18 + k))
      
    for
      i <- 0 until 9
    do
      if playerInv.getItem(i) == box then
        addSlot(new LockedSlot(playerInv, i, 8 + i * 18, 161 + k))
      else
        addSlot(new Slot(playerInv, i, 8 + i * 18, 161 + k))

  override def quickMoveStack(player: Player, slotIndex: Int): ItemStack =
    // copied from code that was ported from java that was ported from scala that was ported from java
    var transferredItemStack = ItemStack.EMPTY
    val slot = this.slots.get(slotIndex)
    if slot.hasItem then
      val slotStack = slot.getItem
      transferredItemStack = slotStack.copy()
      val boxStart = 0
      val boxEnd = boxStart + ToolContainerMenu.containerSize
      val invEnd = boxEnd + ScreenCommon.playerInvSize
      if slotIndex < boxEnd then
        if !moveItemStackTo(slotStack, boxEnd, invEnd, true) then
          return ItemStack.EMPTY
        else if !slotStack.isEmpty && ToolContainerMenu.isValidItem(slotStack) && !moveItemStackTo(slotStack, boxStart, boxEnd, false) then
          return ItemStack.EMPTY

      if slotStack.isEmpty then
        slot.setByPlayer(ItemStack.EMPTY)
      else
        slot.setChanged()

      if slotStack.getCount == transferredItemStack.getCount then
        return ItemStack.EMPTY

      slot.onTake(player, slotStack)
    transferredItemStack

  
  override def stillValid(player: Player): Boolean =
    player.getItemInHand(InteractionHand.MAIN_HAND) == box || player.getItemInHand(InteractionHand.OFF_HAND) == box

object ToolContainerMenu:
  final val rows = 5
  final val columns = 9
  final val containerSize = rows * columns

  def isValidItem(item: ItemStack): Boolean = item.is(BulbusTags.item.validToolTag)

  final class ToolSlot(inv: Container, slot: Int, x: Int, y: Int) extends Slot(inv, slot, x, y):
    override def mayPlace(itemStack: ItemStack): Boolean =
      isValidItem(itemStack)
  
  def fromNetwork(windowId: Int, inv: Inventory, mainHand: Boolean): ToolContainerMenu =
    val hand = if mainHand then InteractionHand.MAIN_HAND else InteractionHand.OFF_HAND
    new ToolContainerMenu(windowId, inv, inv.player.getItemInHand(hand))