package gay.menkissing.bulbus.client.content

import gay.menkissing.bulbus.client.infra.TooltipProviderFor
import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.content.item.{KnowledgeStorageItem, StasisBatteryItem, StasisBottleItem, StasisTubeItem}
import gay.menkissing.bulbus.registries.{BulbusDataComponentTypes, BulbusTranslationKeys}
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidVariant, FluidVariantAttributes}
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

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

  val xpContentProvider: TooltipProviderFor[Long] =
    (self, tooltip, consumer, kind, getter) =>
      val max = KnowledgeStorageItem.getMax(tooltip.registries(), getter)
      consumer(BulbusTranslationKeys.knowledgeStorage.tooltip.showCount(self, max))
  
  def register(): Unit =
    TooltipProviderFor.register(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS)(stasisContentProvider)
    TooltipProviderFor.register(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS)(stasisTubeContentProvider)
    TooltipProviderFor.register(BulbusDataComponentTypes.KNOWLEDGE_STORAGE_CONTENTS)(xpContentProvider)
    ItemTooltipCallback.EVENT.register: (stack, context, kind, tooltip) =>
      // This sucks
      stack.getItem match
        case stasisBatteryItem: StasisBatteryItem =>
          val max = StasisBatteryItem.getMaxStackRegistry(context.registries(), stack)
          tooltip.add(BulbusTranslationKeys.stasisBattery.tooltip.showCount(stasisBatteryItem.getStoredEnergy(stack), max))
          if Minecraft.getInstance().hasShiftDown then
            val transfer = stasisBatteryItem.getEnergyMaxInput(stack)
            tooltip.add(BulbusTranslationKeys.stasisBattery.tooltip.showTransferRate(transfer))
        case _ => ()