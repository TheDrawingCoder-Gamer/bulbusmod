package gay.menkissing.bulbus.client.datagen

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider
import net.minecraft.data.PackOutput
import DatagenPrelude.*
import gay.menkissing.bulbus.registries.BulbusSounds
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder.RegistrationBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.sounds.{SoundEvent, SoundEvents}

class BulbusSoundGenerator(output: FabricPackOutput, registriesFuture: CHLP) extends FabricSoundsProvider(output, registriesFuture):
  def delegated(self: SoundEvent, that: SoundEvent)(using exporter: FabricSoundsProvider.SoundExporter): Unit =
    exporter.add(self,
      SoundTypeBuilder.of(self).sound(RegistrationBuilder.ofEvent(that))
    )

  override def configure(registryLookup: HolderLookup.Provider, exporter: FabricSoundsProvider.SoundExporter): Unit =
    given FabricSoundsProvider.SoundExporter = exporter
    delegated(BulbusSounds.stasisAccessorAddItem, SoundEvents.ITEM_FRAME_ADD_ITEM)
    delegated(BulbusSounds.stasisAccessorRemoveItem, SoundEvents.ITEM_FRAME_REMOVE_ITEM)
    delegated(BulbusSounds.stasisAccessorAddItemFail, SoundEvents.DECORATED_POT_INSERT_FAIL)

    delegated(BulbusSounds.stasisWormOpen, SoundEvents.BARREL_OPEN)
    delegated(BulbusSounds.stasisWormClose, SoundEvents.BARREL_CLOSE)
    
    delegated(BulbusSounds.stasisShelfOpen, SoundEvents.BARREL_OPEN)
    delegated(BulbusSounds.stasisShelfClose, SoundEvents.BARREL_CLOSE)

  override def getName: String = "Bulbus sound generator"
