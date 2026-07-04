package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.content.block.StasisStorageBlock
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.resources.{Identifier, ResourceKey}
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.{Block, Blocks, SoundType}

object BulbusBlocks:
  def register[T <: Block](id: Identifier, factory: BlockBehaviour.Properties => T, props: BlockBehaviour.Properties): T =
    val res = factory(props.setId(ResourceKey.create(Registries.BLOCK, id)))
    Registry.register(BuiltInRegistries.BLOCK, id, res)
  
  val stasisShelf: Block = register(BulbusBlockIds.stasisShelf, StasisStorageBlock.StasisShelfBlock.apply, BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.5f))

  def init(): Unit = ()

