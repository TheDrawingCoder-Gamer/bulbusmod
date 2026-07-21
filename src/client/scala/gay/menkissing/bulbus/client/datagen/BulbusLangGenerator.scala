package gay.menkissing.bulbus.client.datagen

import gay.menkissing.bulbus.registries.*
import gay.menkissing.bulbus.util.resources.*
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup

import java.util.concurrent.CompletableFuture

class BulbusLangGenerator(output: FabricPackOutput, registries: CompletableFuture[HolderLookup.Provider]) extends FabricLanguageProvider(output, "en_us", registries):
  import BulbusLangGenerator.*
  override def generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: FabricLanguageProvider.TranslationBuilder): Unit =
    translationBuilder.addG(BulbusItemIds.stasisBottle, "Stasis Bottle")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.usagePickup, "Use to pickup")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.usagePlace, "Shift-Use to place")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.empty, "Empty")
    translationBuilder.add(BulbusTranslationKeys.stasisBottle.tooltip.countMB, "%1$s mB / %2$s buckets")

    translationBuilder.addG(BulbusItemIds.stasisTube, "Stasis Tube")
    translationBuilder.add(BulbusTranslationKeys.stasisTube.tooltip.empty, "Empty")
    translationBuilder.add(BulbusTranslationKeys.stasisTube.tooltip.count, "%1$d / %2$d (%3$s stacks)")

    translationBuilder.addG(BulbusItemIds.stasisBattery, "Stasis Battery")
    translationBuilder.add(BulbusTranslationKeys.stasisBattery.tooltip.count, "%1$s E / %2$s E")
    translationBuilder.add(BulbusTranslationKeys.stasisBattery.tooltip.transferRate, "Transfer Rate: %1$s E/t")

    translationBuilder.addG(BulbusItemIds.holdingBag, "Holding Bag")

    translationBuilder.addG(BulbusItemIds.toolContainer, "Tool Container")

    translationBuilder.addG(BulbusBlockIds.stasisShelf, "Stasis Shelf")
    
    translationBuilder.addG(BulbusBlockIds.stasisWorm, "Stasis Worm")
    
    translationBuilder.addG(BulbusBlockIds.stasisAccessor, "Stasis Accessor")

    translationBuilder.addG(BulbusBlockIds.repairMachine, "Repair-o-matic")

    translationBuilder.addG(BulbusBlockIds.tunableChest, "Tunable Chest")

    translationBuilder.addG(BulbusBlockIds.tunableTank, "Tunable Tank")

    translationBuilder.addG(BulbusItemIds.knowledgeStorage, "Knowledge Storage")
    translationBuilder.add(BulbusTranslationKeys.knowledgeStorage.tooltip.count, "%1$s / %2$d")

    translationBuilder.add(BulbusTranslationKeys.tab, "Bulbus")
    translationBuilder.add(BulbusTranslationKeys.container.shelf, "Stasis Shelf")
    translationBuilder.add(BulbusTranslationKeys.container.worm, "Stasis Worm")
    translationBuilder.add(BulbusTranslationKeys.container.tunableChest, "Tunable Chest")

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

object BulbusLangGenerator:
  extension(self: FabricLanguageProvider.TranslationBuilder)
    def addG[T](it: T, lang: String)(using hasId: HasDescriptionId[T]): Unit =
      self.add(hasId.getDescriptionId(it), lang)