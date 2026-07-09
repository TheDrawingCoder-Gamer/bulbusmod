package gay.menkissing.bulbus.client.datagen

import gay.menkissing.bulbus.registries.BulbusBlocks
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider
import net.minecraft.core.HolderLookup

import java.util.concurrent.CompletableFuture

//import DatagenPrelude.*


class BulbusLootTableProvider(output: FabricPackOutput, lookup: CompletableFuture[HolderLookup.Provider]) extends FabricBlockLootSubProvider(output, lookup):
  override def generate(): Unit =
    dropSelf(BulbusBlocks.stasisShelf)
    dropSelf(BulbusBlocks.stasisWorm)
    dropSelf(BulbusBlocks.stasisAccessor)
