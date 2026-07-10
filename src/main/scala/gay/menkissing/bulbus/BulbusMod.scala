package gay.menkissing.bulbus

import gay.menkissing.bulbus.content.block.entity.stasis_storage.StasisStorageItemForwarder
import gay.menkissing.bulbus.infra.lookup.StasisStorage
import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusBlocks, BulbusDataComponentTypes, BulbusItems, BulbusScreens, BulbusSounds}
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier

class BulbusMod extends ModInitializer:
  override def onInitialize(): Unit =
    BulbusDataComponentTypes.init()
    BulbusBlocks.init()
    BulbusBlockEntities.init()
    BulbusItems.init()
    StasisStorage.init()
    StasisStorageItemForwarder.init()
    BulbusScreens.init()
    BulbusSounds.init()

object BulbusMod:
  final val MOD_ID: String = "bulbus"
  def locate(name: String): Identifier =
    Identifier.fromNamespaceAndPath(MOD_ID, name)
