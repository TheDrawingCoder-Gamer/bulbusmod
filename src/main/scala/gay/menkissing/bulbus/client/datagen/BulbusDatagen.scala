package gay.menkissing.bulbus.client.datagen

import gay.menkissing.bulbus.client.datagen.models.BulbusModelGenerator
import gay.menkissing.bulbus.client.datagen.tags.BulbusItemTagProvider
import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator}

class BulbusDatagen extends DataGeneratorEntrypoint:
  override def onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator): Unit =
    val pack = fabricDataGenerator.createPack()

    pack.addProvider(BulbusModelGenerator.apply)
    pack.addProvider(BulbusLangGenerator.apply)
    pack.addProvider(BulbusItemTagProvider.apply)

