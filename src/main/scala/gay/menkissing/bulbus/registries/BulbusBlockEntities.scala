package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.content.block.entity.StasisStorageBlockEntity.StasisShelfBlockEntity
import gay.menkissing.bulbus.content.block.entity.{StasisAccessorBlockEntity, StasisStorageBlockEntity, StasisWormBlockEntity}
import gay.menkissing.bulbus.util.LookupUtil.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}

object BulbusBlockEntities:
  def makeEntity[T <: BlockEntity](name: String, factory: FabricBlockEntityTypeBuilder.Factory[T], blocks: Block*): BlockEntityType[T] =
    val id = BulbusMod.locate(name)
    Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.create(factory, blocks*).build())

  val stasisShelf: BlockEntityType[StasisShelfBlockEntity] = makeEntity("stasis_shelf", StasisShelfBlockEntity.apply, BulbusBlocks.stasisShelf)
  val stasisWorm: BlockEntityType[StasisWormBlockEntity] = makeEntity("stasis_worm", StasisWormBlockEntity.apply, BulbusBlocks.stasisWorm)
  val stasisAccessor: BlockEntityType[StasisAccessorBlockEntity] = makeEntity("stasis_accessor", StasisAccessorBlockEntity.apply, BulbusBlocks.stasisAccessor)
  
  def init(): Unit =
    ItemStorage.SIDED.registerForBlockEntities(
      (a, _) => a.asInstanceOf[StasisStorageBlockEntity].itemStorage,
      stasisShelf,
      stasisWorm
    )
    FluidStorage.SIDED.registerForBlockEntities(
      (a, _) => a.asInstanceOf[StasisStorageBlockEntity].fluidStorage,
      stasisShelf,
      stasisWorm,
      stasisAccessor
    )

    // i dont really see a need to limit insertion/extraction
    // Go nuts, kids
    ItemStorage.SIDED.registerScalaEntities(stasisAccessor): (ent, dir) =>
      dir match
        case Direction.UP => ent.itemStorage
        case Direction.DOWN => ent.itemStorage
        case _ => ent.containerStorage


