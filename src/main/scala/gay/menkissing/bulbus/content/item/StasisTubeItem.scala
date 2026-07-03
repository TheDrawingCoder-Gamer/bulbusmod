package gay.menkissing.bulbus.content.item

import gay.menkissing.bulbus.components.StorageItemContents
import StorageItemContents.Item.ext.*
import gay.menkissing.bulbus.registries.{BulbusDataComponentTypes, BulbusTags}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.{ClickAction, Slot}
import net.minecraft.world.item.{Item, ItemStack}

class StasisTubeItem(props: Item.Properties) extends Item(props):
  override def overrideStackedOnOther(self: ItemStack, slot: Slot, clickAction: ClickAction, player: Player): Boolean =
    if clickAction == ClickAction.SECONDARY then
      val thatStack = slot.getItem
      val contents = self.getOrDefault(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS, StorageItemContents.Item.DEFAULT)
      // todo, max
      val builder = StorageItemContents.Item.builder(contents, StasisTubeItem.baseMax)
      if thatStack.isEmpty then
        val stack = builder.removeStack()
        if !stack.isEmpty then
          val remainder = slot.safeInsert(stack)
          builder.insertStack(remainder)
      else
        if StasisTubeItem.validStack(thatStack) then
          builder.addFromSlot(slot, player)
      self.set(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS, builder.result)
      true
    else
      false

  override def overrideOtherStackedOnMe(self: ItemStack, other: ItemStack, slot: Slot, clickAction: ClickAction, player: Player, carriedItem: SlotAccess): Boolean =
    if clickAction == ClickAction.SECONDARY && slot.allowModification(player) then
      val contents = self.getOrDefault(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS, StorageItemContents.Item.DEFAULT)
      // todo, max
      val builder = StorageItemContents.Item.builder(contents, StasisTubeItem.baseMax)
      if other.isEmpty then
        if !builder.isEmpty then
          val removed = builder.removeStack()
          if !removed.isEmpty then
            carriedItem.set(removed)
      else if StasisTubeItem.validStack(other) then
        builder.insertStack(other)

      self.set(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS, builder.result)
      true
    else
      false

object StasisTubeItem:
  val baseMax: Long = 20_000

  def validStack(stack: ItemStack): Boolean =
    stack.getItem.canFitInsideContainerItems && !stack.is(BulbusTags.item.tubeBlacklist)
  def getStoredItem(stack: ItemStack): Option[ItemVariant] =
    val r = stack.get(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS)
    if r != null then
      Option.when(!r.isEmpty)(r.variant)
    else
      None

  final class StasisTubeStorage(context: ContainerItemContext, val capacity: Long) extends SingleVariantItemStorage[ItemVariant](context):
    override def getCapacity(variant: ItemVariant): Long = capacity

    override def getBlankResource: ItemVariant = ItemVariant.blank()

    override def getUpdatedVariant(currentVariant: ItemVariant, newResource: ItemVariant, newAmount: Long): ItemVariant =
      val newContents = StorageItemContents[ItemVariant](newResource, newAmount)

      currentVariant.withComponents(DataComponentPatch.builder().set(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS, newContents).build())

    override def getResource(currentVariant: ItemVariant): ItemVariant =
      val contents = currentVariant.get(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS)
      if contents != null then
        contents.variant
      else
        ItemVariant.blank()

    override def getAmount(currentVariant: ItemVariant): Long =
      val contents = currentVariant.get(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS)
      if contents != null then
        contents.amount
      else
        0