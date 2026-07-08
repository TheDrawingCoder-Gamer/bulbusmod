package gay.menkissing.bulbus.client.datagen

import gay.menkissing.bulbus.registries.{BulbusItems, BulbusTranslationKeys}
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

    translationBuilder.add(BulbusItems.holdingBag, "Holding Bag")

    translationBuilder.add(BulbusItems.toolContainer, "Tool Container")

    translationBuilder.add(BulbusItems.stasisShelf, "Stasis Shelf")
    
    translationBuilder.add(BulbusItems.stasisWorm, "Stasis Worm")
    
    translationBuilder.add(BulbusItems.stasisAccessor, "Stasis Accessor")

    translationBuilder.add(BulbusTranslationKeys.tab, "Bulbus")
    translationBuilder.add(BulbusTranslationKeys.container.shelf, "Stasis Shelf")
    translationBuilder.add(BulbusTranslationKeys.container.worm, "Stasis Worm")