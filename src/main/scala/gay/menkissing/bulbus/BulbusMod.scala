package gay.menkissing.bulbus

import gay.menkissing.bulbus.content.block.entity.stasis_storage.StasisStorageItemForwarder
import gay.menkissing.bulbus.infra.lookup.StasisStorage
import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusBlocks, BulbusBuiltInRegistries, BulbusDataComponentTypes, BulbusItemForwarders, BulbusItems, BulbusRegistries, BulbusScreens, BulbusSounds}
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import gay.menkissing.bulbus.registries.BulbusParticles
import gay.menkissing.bulbus.registries.BulbusComponentPredicates
import gay.menkissing.bulbus.registries.BulbusEntities

class BulbusMod extends ModInitializer:
  override def onInitialize(): Unit =
    BulbusDataComponentTypes.init()
    BulbusComponentPredicates.init()
    BulbusBlocks.init()
    BulbusBlockEntities.init()
    BulbusEntities.init()
    BulbusItems.init()
    StasisStorage.init()
    BulbusBuiltInRegistries.init()
    BulbusItemForwarders.init()
    BulbusScreens.init()
    BulbusSounds.init()
    BulbusParticles.init()

object BulbusMod:
  final val MOD_ID: String = "bulbus"
  def locate(name: String): Identifier =
    Identifier.fromNamespaceAndPath(MOD_ID, name)
