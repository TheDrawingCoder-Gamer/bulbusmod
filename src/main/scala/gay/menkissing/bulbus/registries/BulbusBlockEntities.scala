package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.content.block.entity.StasisStorageBlockEntity.StasisShelfBlockEntity
import gay.menkissing.bulbus.content.block.entity.stasis_storage.StasisStorageItemForwarder
import gay.menkissing.bulbus.content.block.entity.{RepairMachineBlockEntity, StasisAccessorBlockEntity, StasisStorageBlockEntity, StasisWormBlockEntity}
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
import team.reborn.energy.api.EnergyStorage

object BulbusBlockEntities:
  def makeEntity[T <: BlockEntity](name: String, factory: FabricBlockEntityTypeBuilder.Factory[T], blocks: Block*): BlockEntityType[T] =
    val id = BulbusMod.locate(name)
    Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.create(factory, blocks*).build())

  val stasisShelf: BlockEntityType[StasisShelfBlockEntity] = makeEntity("stasis_shelf", StasisShelfBlockEntity.apply, BulbusBlocks.stasisShelf)
  val stasisWorm: BlockEntityType[StasisWormBlockEntity] = makeEntity("stasis_worm", StasisWormBlockEntity.apply, BulbusBlocks.stasisWorm)
  val stasisAccessor: BlockEntityType[StasisAccessorBlockEntity] = makeEntity("stasis_accessor", StasisAccessorBlockEntity.apply, BulbusBlocks.stasisAccessor)

  val repairMachine: BlockEntityType[RepairMachineBlockEntity] = makeEntity("repair_machine", RepairMachineBlockEntity.apply, BulbusBlocks.repairMachine)

  def init(): Unit =
    ItemStorage.SIDED.registerScalaEntities[StasisStorageBlockEntity](stasisShelf, stasisWorm): (ent, _) =>
      ent.getForwardedStorage(BulbusItemForwarders.forItem)
    FluidStorage.SIDED.registerScalaEntities[StasisStorageBlockEntity](stasisShelf, stasisWorm, stasisAccessor): (ent, _) =>
      ent.getForwardedStorage(BulbusItemForwarders.forFluid)

    EnergyStorage.SIDED.registerScalaEntities[StasisStorageBlockEntity](stasisShelf, stasisWorm, stasisAccessor): (ent, _) =>
      ent.getForwardedStorage(BulbusItemForwarders.forEnergy)

    // i dont really see a need to limit insertion/extraction
    // Go nuts, kids
    ItemStorage.SIDED.registerScalaEntities(stasisAccessor): (ent, dir) =>
      dir match
        case Direction.UP | Direction.DOWN => ent.getForwardedStorage(BulbusItemForwarders.forItem)
        case _ => ent.containerStorage


