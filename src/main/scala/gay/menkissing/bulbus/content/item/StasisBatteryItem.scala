package gay.menkissing.bulbus.content.item

import gay.menkissing.bulbus.registries.BulbusTranslationKeys
import gay.menkissing.bulbus.util.BulbusEnchantmentUtil
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.minecraft.core.{Holder, HolderLookup}
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.enchantment.{Enchantment, Enchantments}
import net.minecraft.world.item.{Item, ItemStack, TooltipFlag}
import team.reborn.energy.api.base.SimpleEnergyItem

import java.util.function.Consumer

final class StasisBatteryItem(props: Item.Properties) extends Item(props), SimpleEnergyItem:
  override def canBeEnchantedWith(stack: ItemStack, enchantment: Holder[Enchantment], context: EnchantingContext): Boolean =
    super.canBeEnchantedWith(stack, enchantment, context) || enchantment.is(Enchantments.POWER) || enchantment.is(Enchantments.QUICK_CHARGE)

  override def getEnergyCapacity(stack: ItemStack): Long = StasisBatteryItem.getMaxEvil(stack)

  override def getEnergyMaxInput(stack: ItemStack): Long = StasisBatteryItem.getTransferEvil(stack)

  override def getEnergyMaxOutput(stack: ItemStack): Long = StasisBatteryItem.getTransferEvil(stack)


object StasisBatteryItem:
  val baseMax: Long = 20_000
  val baseTransfer: Long = 32

  def getTransfer(level: Int): Long =
    baseTransfer * math.pow(2, math.min(level, 10)).toLong

  def getTransferEvil(stack: DataComponentGetter): Long =
    getTransfer(BulbusEnchantmentUtil.getLevelEvil(Enchantments.QUICK_CHARGE, stack))

  def getMax(level: Int): Long =
    baseMax * math.pow(10, math.min(level, 5)).toLong

  def getMaxStackRegistry(lookup: HolderLookup.Provider, stack: DataComponentGetter): Long =
    getMax(BulbusEnchantmentUtil.getLevel(lookup, Enchantments.POWER, stack))

  def getMaxEvil(stack: DataComponentGetter): Long =
    getMax(BulbusEnchantmentUtil.getLevelEvil(Enchantments.POWER, stack))
