package gay.menkissing.bulbus

import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusBlocks, BulbusDataComponentTypes, BulbusItems, BulbusScreens}
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier

class BulbusMod extends ModInitializer:
  override def onInitialize(): Unit =
    BulbusDataComponentTypes.init()
    BulbusBlocks.init()
    BulbusBlockEntities.init()
    BulbusItems.init()
    BulbusScreens.init()

object BulbusMod:
  final val MOD_ID: String = "bulbus"
  def locate(name: String): Identifier =
    Identifier.fromNamespaceAndPath(MOD_ID, name)
