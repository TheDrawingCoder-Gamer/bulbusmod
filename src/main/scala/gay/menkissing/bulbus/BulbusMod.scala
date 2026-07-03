package gay.menkissing.bulbus

import gay.menkissing.bulbus.registries.{BulbusDataComponentTypes, BulbusItems}
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier

class BulbusMod extends ModInitializer:
  override def onInitialize(): Unit =
    BulbusDataComponentTypes.init()
    BulbusItems.init()

object BulbusMod:
  val MOD_ID: String = "bulbus"
  def locate(name: String): Identifier =
    Identifier.fromNamespaceAndPath(MOD_ID, name)