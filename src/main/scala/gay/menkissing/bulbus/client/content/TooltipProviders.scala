package gay.menkissing.bulbus.client.content

import gay.menkissing.bulbus.client.infra.TooltipProviderFor
import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.content.item.{StasisBottleItem, StasisTubeItem}
import gay.menkissing.bulbus.registries.{BulbusDataComponentTypes, BulbusTranslationKeys}
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidVariant, FluidVariantAttributes}
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
object TooltipProviders:
  val stasisContentProvider: TooltipProviderFor[StorageItemContents[FluidVariant]] =
    (self, tooltip, consumer, kind, getter) =>
      if self.isEmpty then
        consumer(Component.translatable(BulbusTranslationKeys.stasisBottle.tooltip.empty))
        consumer(Component.translatable(BulbusTranslationKeys.stasisBottle.tooltip.usagePickup))
      else
        val max = StasisBottleItem.getMaxFromWorld(getter, tooltip.registries())
        consumer(BulbusTranslationKeys.stasisBottle.tooltip.showCountMB(self.amount, max))
        consumer(FluidVariantAttributes.getName(self.variant))
        consumer(Component.translatable(BulbusTranslationKeys.stasisBottle.tooltip.usagePickup))
        consumer(Component.translatable(BulbusTranslationKeys.stasisBottle.tooltip.usagePlace))

  val stasisTubeContentProvider: TooltipProviderFor[StorageItemContents[ItemVariant]] =
    (self, tooltip, consumer, kind, getter) =>
      if self.isEmpty then
        consumer(Component.translatable(BulbusTranslationKeys.stasisTube.tooltip.empty))
      else
        val contained = self.variant.toStack
        val max = StasisTubeItem.maxFromWorld(getter, tooltip.registries())
        val totalStacks = math.floorDiv(self.amount, contained.getMaxStackSize).toString
        consumer(BulbusTranslationKeys.stasisTube.tooltip.showCount(self.amount, max, totalStacks))
        consumer(contained.getHoverName)

  def register(): Unit =
    TooltipProviderFor.register(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS)(stasisContentProvider)
    TooltipProviderFor.register(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS)(stasisTubeContentProvider)