package gay.menkissing.bulbus.client.datagen.tags

import gay.menkissing.bulbus.registries.{BulbusItems, BulbusTags}
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.Items

import java.util.concurrent.CompletableFuture

class BulbusItemTagProvider(output: FabricPackOutput, registriesFuture: CompletableFuture[HolderLookup.Provider]) extends FabricTagsProvider.ItemTagsProvider(output, registriesFuture):
  override def addTags(registries: HolderLookup.Provider): Unit =
    valueLookupBuilder(BulbusTags.item.tubeBlacklist)
      .add(BulbusItems.stasisTube)

    valueLookupBuilder(BulbusTags.item.validToolTag)
      .addOptionalTag(ConventionalItemTags.TOOLS)
    
    valueLookupBuilder(BulbusTags.item.voidsInsertInShelf)
      .add(Items.LAVA_BUCKET)
      .add(Items.CACTUS)