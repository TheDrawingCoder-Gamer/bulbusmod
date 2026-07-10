package gay.menkissing.bulbus.client.datagen.tags

import gay.menkissing.bulbus.registries.BulbusBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags

import java.util.concurrent.CompletableFuture

class BulbusBlockTagProvider(output: FabricPackOutput, lookup: CompletableFuture[HolderLookup.Provider]) extends FabricTagsProvider.BlockTagsProvider(output, lookup):
  override def addTags(registries: HolderLookup.Provider): Unit =
    valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
      .add(BulbusBlocks.stasisShelf)
      .add(BulbusBlocks.stasisWorm)
      .add(BulbusBlocks.stasisAccessor)
