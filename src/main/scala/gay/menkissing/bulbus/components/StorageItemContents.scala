package gay.menkissing.bulbus.components

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.{ByteBufCodecs, StreamCodec}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

import gay.menkissing.bulbus.util.dfu.{*, given}

import cats.*
import cats.implicits.*

final case class StorageItemContents[T <: TransferVariant[?]](variant: T, amount: Long):
  def isEmpty: Boolean =
    variant.isBlank || amount <= 0


object StorageItemContents:
  def codecFor[T <: TransferVariant[?]](variantCodec: Codec[T]): Codec[StorageItemContents[T]] =
    simpleBuiltCodec[StorageItemContents[T]]:
      _.map2(
        variantCodec.fieldOf("variant").forGetter(_.variant),
        Codec.LONG.fieldOf("amount").forGetter(_.amount)
      )(StorageItemContents.apply)

  def streamCodecFor[T <: TransferVariant[?]](variantCodec: StreamCodec[RegistryFriendlyByteBuf, T]): StreamCodec[RegistryFriendlyByteBuf, StorageItemContents[T]] =
    StreamCodec.composite(
      variantCodec, _.variant,
      ByteBufCodecs.LONG, _.amount,
      StorageItemContents.apply
    )

  object Fluid:
    val CODEC: Codec[StorageItemContents[FluidVariant]] = codecFor(FluidVariant.CODEC)
    val STREAM_CODEC: StreamCodec[RegistryFriendlyByteBuf, StorageItemContents[FluidVariant]] = streamCodecFor(FluidVariant.PACKET_CODEC)

    def builder(from: StorageItemContents[FluidVariant], max: Long): Builder[FluidVariant] =
      new Builder[FluidVariant](from.variant, from.amount, max, FluidVariant.blank())
      
    val DEFAULT: StorageItemContents[FluidVariant] = StorageItemContents(FluidVariant.blank(), 0)

  object Item:
    val CODEC: Codec[StorageItemContents[ItemVariant]] = codecFor(ItemVariant.CODEC)
    val STREAM_CODEC: StreamCodec[RegistryFriendlyByteBuf, StorageItemContents[ItemVariant]] = streamCodecFor(ItemVariant.PACKET_CODEC)
    
    def builder(from: StorageItemContents[ItemVariant], max: Long): Builder[ItemVariant] =
      new Builder[ItemVariant](from.variant, from.amount, max, ItemVariant.blank())

    object ext:
      extension (self: Builder[ItemVariant])
        def getMaxAllowedStack(stack: ItemStack): Int =
          math.min(self.getMaxAllowed(ItemVariant.of(stack), stack.getCount), Int.MaxValue).toInt
  
        def insertStack(stack: ItemStack): Int =
          val added = math.min(stack.getCount, self.getMaxAllowedStack(stack))
          if added == 0 then
            return 0
  
          if self.amount == 0 then
            self.template = ItemVariant.of(stack)
  
          self.amount += math.min(self.max - self.amount, added)
          stack.shrink(added)
          added
  
        def addFromSlot(slot: Slot, player: Player): Long =
          val stack = slot.getItem
          val i = self.getMaxAllowedStack(stack)
          self.insertStack(slot.safeTake(slot.getItem.getCount, i, player))
        
        def remove(amount: Int): ItemStack =
          if self.isEmpty then
            ItemStack.EMPTY
          else
            val toRemove = math.min(self.amount, amount).toInt
            val removed = self.template.toStack(toRemove)
            self.amount -= toRemove
            if self.amount == 0 then
              self.template = ItemVariant.blank()
            
            removed
        
        def removeStack(): ItemStack =
          remove(self.template.toStack.getMaxStackSize)


    val DEFAULT: StorageItemContents[ItemVariant] = StorageItemContents(ItemVariant.blank(), 0)

  final class Builder[T <: TransferVariant[?]](var template: T, var amount: Long, val max: Long, val blank: T):
    def isEmpty: Boolean =
      template.isBlank || amount <= 0

    def result: StorageItemContents[T] =
      StorageItemContents[T](template, amount)

    def getMaxAllowed(variant: T, amount: Long): Long =
      if variant.isBlank || amount <= 0 || (!this.isEmpty && template != variant) then
        0
      else
        this.max - this.amount

    def insert(variant: T, amount: Long): Long =
      val added = math.min(amount, getMaxAllowed(variant, amount))
      if added == 0 then
        0
      else
        if this.isEmpty then
          this.template = variant

        val forEel = math.min(this.max - this.amount, added)
        this.amount += forEel
        forEel

    def extract(variant: T, amount: Long): Long =
      if variant != template then
        0
      else
        val toRemove = math.min(this.amount, amount)
        this.amount -= toRemove
        if this.amount == 0 then
          this.template = blank

        toRemove

