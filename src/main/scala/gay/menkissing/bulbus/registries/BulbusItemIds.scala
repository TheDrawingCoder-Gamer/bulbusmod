package gay.menkissing.bulbus.registries

import net.minecraft.resources.Identifier
import gay.menkissing.bulbus.BulbusMod

object BulbusItemIds:
  opaque type ItemIdentifier <: Identifier = Identifier
  object ItemIdentifier:
    extension (self: ItemIdentifier)
      def descriptionId: String = s"item.${self.getNamespace}.${self.getPath.replace("/", ".")}"

  val stasisBottle: ItemIdentifier = BulbusMod.locate("stasis_bottle")
  val stasisTube: ItemIdentifier = BulbusMod.locate("stasis_tube")
  val stasisBattery: ItemIdentifier = BulbusMod.locate("stasis_battery")
  val holdingBag: ItemIdentifier = BulbusMod.locate("holding_bag")
  val toolContainer: ItemIdentifier = BulbusMod.locate("tool_container")
  val knowledgeStorage: ItemIdentifier = BulbusMod.locate("knowledge_storage")
  val repairBottle: ItemIdentifier = BulbusMod.locate("repair_bottle")