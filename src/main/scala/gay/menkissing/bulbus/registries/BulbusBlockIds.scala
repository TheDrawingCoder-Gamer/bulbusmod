package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import net.minecraft.resources.Identifier

object BulbusBlockIds:
  opaque type BlockIdentifier <: Identifier = Identifier
  object BlockIdentifier:
    extension (self: BlockIdentifier)
      def descriptionId: String = s"block.${self.getNamespace}.${self.getPath.replace("/", ".")}"

  val stasisShelf: BlockIdentifier = BulbusMod.locate("stasis_shelf")
  val stasisWorm: BlockIdentifier = BulbusMod.locate("stasis_worm")
  val stasisAccessor: BlockIdentifier = BulbusMod.locate("stasis_accessor")

  val repairMachine: BlockIdentifier = BulbusMod.locate("repair_machine")
  val tunableChest: BlockIdentifier = BulbusMod.locate("tunable_chest")
  val tunableTank: BlockIdentifier = BulbusMod.locate("tunable_tank")
