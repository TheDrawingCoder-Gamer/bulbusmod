package gay.menkissing.bulbus.screen

import gay.menkissing.bulbus.registries.{BulbusItems, BulbusScreens}
import net.minecraft.world.{Container, SimpleContainer}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{ChestMenu, ContainerInput}
import net.minecraft.world.item.ItemStack

class HoldingBagMenu(syncId: Int, inventory: Inventory, container: Container) extends ChestMenu(BulbusScreens.holdingBagMenu, syncId, inventory, container, 3):

  override def clicked(slotIndex: Int, buttonNum: Int, containerInput: ContainerInput, player: Player): Unit =
    if slotIndex > 0 && isValidSlotIndex(slotIndex) && this.slots.get(slotIndex).getItem.is(BulbusItems.holdingBag) then
      return
    super.clicked(slotIndex, buttonNum, containerInput, player)

  override def quickMoveStack(player: Player, slotIndex: Int): ItemStack =
    if slotIndex > 0 && isValidSlotIndex(slotIndex) && this.slots.get(slotIndex).getItem.is(BulbusItems.holdingBag) then
      return ItemStack.EMPTY
    super.quickMoveStack(player, slotIndex)

object HoldingBagMenu:
  def fromNetwork(syncId: Int, inventory: Inventory): HoldingBagMenu =
    new HoldingBagMenu(syncId, inventory, new SimpleContainer(9 * 3))