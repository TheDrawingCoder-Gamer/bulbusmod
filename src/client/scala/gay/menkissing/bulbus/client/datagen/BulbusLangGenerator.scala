package gay.menkissing.bulbus.client.datagen

import gay.menkissing.bulbus.registries.{BulbusItems, BulbusSounds, BulbusTags, BulbusTranslationKeys}
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup

import java.util.concurrent.CompletableFuture

class BulbusLangGenerator(output: FabricPackOutput, registries: CompletableFuture[HolderLookup.Provider]) extends FabricLanguageProvider(output, "en_us", registries):
  override def generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: FabricLanguageProvider.TranslationBuilder): Unit =
    translationBuilder.add(BulbusItems.stasisBottle, "Stasis Bottle")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.usagePickup, "Use to pickup")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.usagePlace, "Shift-Use to place")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.empty, "Empty")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.countMB, "%1$s mB / %2$s buckets")

    translationBuilder.add(BulbusItems.stasisTube, "Stasis Tube")
    translationBuilder.add(BulbusTranslationKeys.stasisTube.tooltip.empty, "Empty")
    translationBuilder.add(BulbusTranslationKeys.stasisTube.tooltip.count, "%1$d / %2$d (%3$s stacks)")

    translationBuilder.add(BulbusItems.stasisBattery, "Stasis Battery")
    translationBuilder.add(BulbusTranslationKeys.stasisBattery.tooltip.count, "%1$s E / %2$s E")
    translationBuilder.add(BulbusTranslationKeys.stasisBattery.tooltip.transferRate, "Transfer Rate: %1$s E/t")

    translationBuilder.add(BulbusItems.holdingBag, "Holding Bag")

    translationBuilder.add(BulbusItems.toolContainer, "Tool Container")

    translationBuilder.add(BulbusItems.stasisShelf, "Stasis Shelf")
    
    translationBuilder.add(BulbusItems.stasisWorm, "Stasis Worm")
    
    translationBuilder.add(BulbusItems.stasisAccessor, "Stasis Accessor")

    translationBuilder.add(BulbusItems.knowledgeStorage, "Knowledge Storage")
    translationBuilder.add(BulbusTranslationKeys.knowledgeStorage.tooltip.count, "%1$s / %2$d")

    translationBuilder.add(BulbusTranslationKeys.tab, "Bulbus")
    translationBuilder.add(BulbusTranslationKeys.container.shelf, "Stasis Shelf")
    translationBuilder.add(BulbusTranslationKeys.container.worm, "Stasis Worm")

    translationBuilder.add(BulbusSounds.stasisAccessorAddItem, "Stasis Accessor fills")
    translationBuilder.add(BulbusSounds.stasisAccessorRemoveItem, "Stasis Accessor empties")
    translationBuilder.add(BulbusSounds.stasisAccessorAddItemFail, "Stasis Accessor bonks")

    translationBuilder.add(BulbusSounds.stasisWormOpen, "Stasis Worm opens")
    translationBuilder.add(BulbusSounds.stasisWormClose, "Stasis Worm closes")
    translationBuilder.add(BulbusSounds.stasisShelfOpen, "Stasis Shelf opens")
    translationBuilder.add(BulbusSounds.stasisShelfClose, "Stasis Shelf closes")

    translationBuilder.add(BulbusTags.item.voidsInsertInShelf, "Voids inserts in Shelf")
    translationBuilder.add(BulbusTags.item.validToolTag, "Valid Tools")
    translationBuilder.add(BulbusTags.item.tubeBlacklist, "Stasis Tube Blacklist")
