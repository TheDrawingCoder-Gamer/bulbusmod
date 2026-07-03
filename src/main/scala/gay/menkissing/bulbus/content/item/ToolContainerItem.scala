package gay.menkissing.bulbus.content.item

import gay.menkissing.bulbus.inventory.ItemBackedInventory
import gay.menkissing.bulbus.registries.{BulbusScreens, BulbusTags}
import gay.menkissing.bulbus.screen.ToolContainerMenu
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.{InteractionHand, InteractionResult, SimpleContainer}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, ClickAction, Slot}
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.item.{Item, ItemStack, ItemUtils}
import net.minecraft.world.level.Level

class ToolContainerItem(props: Item.Properties) extends Item(props):
  override def allowComponentsUpdateAnimation(player: Player, hand: InteractionHand, oldStack: ItemStack, newStack: ItemStack): Boolean =
    player.containerMenu match
      case _: ToolContainerMenu => false
      case _ => true

  override def use(level: Level, player: Player, hand: InteractionHand): InteractionResult =
    if !level.isClientSide then
      val stack = player.getItemInHand(hand)
      val provider = new ExtendedMenuProvider[Boolean]:
        override def createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
          BulbusScreens.toolContainer.create(containerId, inventory, hand == InteractionHand.MAIN_HAND)

        override def getDisplayName: Component = stack.getHoverName

        override def getScreenOpeningData(player: ServerPlayer): Boolean =
          hand == InteractionHand.MAIN_HAND

      player.openMenu(provider)
    InteractionResult.SUCCESS

  override def onDestroyed(itemEntity: ItemEntity): Unit =
    ItemUtils.onContainerDestroyed(itemEntity, itemEntity.getItem.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream())
    itemEntity.getItem.remove(DataComponents.CONTAINER)

  override def overrideOtherStackedOnMe(self: ItemStack, other: ItemStack, slot: Slot, clickAction: ClickAction, player: Player, carriedItem: SlotAccess): Boolean =
    if clickAction == ClickAction.SECONDARY && slot.allowModification(player) && !other.isEmpty && ToolContainerMenu.isValidItem(other) then
      val container = ToolContainerItem.getRawInventory(self)
      if container.canAddItem(other) then
        val res = container.addItem(other)
        carriedItem.set(res)
        return true
    false

  override def overrideStackedOnOther(self: ItemStack, slot: Slot, clickAction: ClickAction, player: Player): Boolean =
    if clickAction != ClickAction.SECONDARY then
      false
    else
      val container = ToolContainerItem.getRawInventory(self)
      val other = slot.getItem
      if !other.isEmpty && ToolContainerMenu.isValidItem(other) && container.canAddItem(other) then
        val res = container.addItem(other)
        slot.set(res)
        true
      else
        false

object ToolContainerItem:
  def getRawInventory(stack: ItemStack): SimpleContainer =
    new ItemBackedInventory(stack, ToolContainerMenu.containerSize)

