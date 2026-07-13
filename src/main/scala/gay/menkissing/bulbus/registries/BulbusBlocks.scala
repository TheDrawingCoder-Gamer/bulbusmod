package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.content.block.{RepairMachineBlock, StasisAccessorBlock, StasisStorageBlock}
import gay.menkissing.bulbus.content.block.StasisStorageBlock.StasisWormBlock
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.resources.{Identifier, ResourceKey}
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.{Block, Blocks, SoundType}
import gay.menkissing.bulbus.content.block.TunableChestBlock
import gay.menkissing.bulbus.content.block.TunableTankBlock

object BulbusBlocks:
  def register[T <: Block](id: Identifier, factory: BlockBehaviour.Properties => T, props: BlockBehaviour.Properties): T =
    val res = factory(props.setId(ResourceKey.create(Registries.BLOCK, id)))
    Registry.register(BuiltInRegistries.BLOCK, id, res)
  
  val stasisShelf: Block = register(BulbusBlockIds.stasisShelf, StasisStorageBlock.StasisShelfBlock.apply, BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.5f))
  val stasisWorm: Block = register(BulbusBlockIds.stasisWorm, StasisWormBlock.apply, BlockBehaviour.Properties.of().sound(SoundType.GRASS).strength(1.5f))
  val stasisAccessor: Block = register(BulbusBlockIds.stasisAccessor, StasisAccessorBlock.apply, BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.5f).noOcclusion())

  val repairMachine: Block = register(BulbusBlockIds.repairMachine, RepairMachineBlock.apply, BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(2.0f).noOcclusion())

  val tunableChest: Block = register(BulbusBlockIds.tunableChest, TunableChestBlock.apply, BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(2.0f).noOcclusion())
  val tunableTank: Block = register(BulbusBlockIds.tunableTank, TunableTankBlock.apply, BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(2.0f).noOcclusion())
  
  def init(): Unit = ()

