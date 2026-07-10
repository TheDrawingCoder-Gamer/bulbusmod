package gay.menkissing.bulbus.client.datagen

import net.minecraft.core.HolderLookup

import java.util.concurrent.CompletableFuture

object DatagenPrelude:
  export net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
  export net.minecraft.core.HolderLookup

  type HLP = HolderLookup.Provider
  type CHLP = CompletableFuture[HLP]
