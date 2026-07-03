package gay.menkissing.bulbus.util

import net.minecraft.core.HolderLookup
import net.minecraft.core.component.{DataComponentGetter, DataComponents}
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.{Enchantment, EnchantmentHelper, ItemEnchantments}

object BulbusEnchantmentUtil:
  def getLevel(lookup: HolderLookup.Provider, key: ResourceKey[Enchantment], stack: DataComponentGetter): Int =
    lookup.lookup(Registries.ENCHANTMENT)
          .flatMap(_.get(key))
          .map: entry => 
            val poopie = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
            poopie.getLevel(entry)
          .orElse(0)

  def getLevelEvil(key: ResourceKey[Enchantment], stack: DataComponentGetter): Int =
    stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
         .entrySet()
         .stream()
         .filter(_.getKey.is(key))
         .map(_.getIntValue)
         .findFirst()
         .orElse(0)
