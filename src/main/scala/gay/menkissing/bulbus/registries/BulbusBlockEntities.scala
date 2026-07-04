package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.content.block.entity.StasisStorageBlockEntity.StasisShelfBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
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
  
  def init(): Unit =
    ItemStorage.SIDED.registerForBlockEntity[StasisShelfBlockEntity](
      (a, _) => a.itemStorage,
      stasisShelf
    )
    FluidStorage.SIDED.registerForBlockEntity[StasisShelfBlockEntity](
      (a, _) => a.fluidStorage,
      stasisShelf
    )
