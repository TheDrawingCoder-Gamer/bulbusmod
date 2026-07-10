package gay.menkissing.bulbus.client.content.color.item

import com.mojang.serialization.MapCodec
import gay.menkissing.bulbus.registries.BulbusDataComponentTypes
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.minecraft.client.color.item.ItemTintSource
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object BottleContentsTint extends ItemTintSource:
  val MAP_CODEC: MapCodec[BottleContentsTint.type] = MapCodec.unit(this)

  override def calculate(itemStack: ItemStack, level: ClientLevel, owner: LivingEntity): Int =
    val contents = itemStack.get(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS)
    if contents != null && !contents.isEmpty then
      val fluid = contents.variant
      FluidVariantRendering.getColor(fluid)
    else
      -1


  override def `type`(): MapCodec[? <: ItemTintSource] = MAP_CODEC
