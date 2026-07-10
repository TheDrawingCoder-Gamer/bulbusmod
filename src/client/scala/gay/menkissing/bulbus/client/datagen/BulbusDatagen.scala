package gay.menkissing.bulbus.client.datagen

import gay.menkissing.bulbus.client.datagen.models.BulbusModelGenerator
import gay.menkissing.bulbus.client.datagen.tags.{BulbusBlockTagProvider, BulbusItemTagProvider}
import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator}

class BulbusDatagen extends DataGeneratorEntrypoint:
  override def onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator): Unit =
    val pack = fabricDataGenerator.createPack()

    pack.addProvider(BulbusModelGenerator.apply)
    pack.addProvider(BulbusLangGenerator.apply)
    pack.addProvider(BulbusItemTagProvider.apply)
    pack.addProvider(BulbusBlockTagProvider.apply)
    pack.addProvider(BulbusLootTableProvider.apply)
    pack.addProvider(BulbusSoundGenerator.apply)
    

