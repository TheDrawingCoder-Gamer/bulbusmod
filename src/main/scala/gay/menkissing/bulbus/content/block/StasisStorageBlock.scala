package gay.menkissing.bulbus.content.block

import com.mojang.serialization.MapCodec
import gay.menkissing.bulbus.content.block.entity.{ContainerStasisStorageBlockEntity, StasisStorageBlockEntity}
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.{Containers, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.{BarrelBlock, BaseEntityBlock, Block}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult

abstract class StasisStorageBlock(val capacity: Int, props: BlockBehaviour.Properties) extends BaseEntityBlock(props):
  locally:
    this.registerDefaultState:
      this.stateDefinition.any()
          .setValue(BlockStateProperties.OPEN, false)

  override protected def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(BlockStateProperties.OPEN)

  override def useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult =
    if level.isClientSide then
      InteractionResult.SUCCESS
    else
      val blockEntity = level.getBlockEntity(pos)
      blockEntity match
        case bse: ContainerStasisStorageBlockEntity =>
          player.openMenu(bse)
        case _ => ()
      InteractionResult.CONSUME

  override def affectNeighborsAfterRemoval(state: BlockState, level: ServerLevel, pos: BlockPos, movedByPiston: Boolean): Unit =
    Containers.updateNeighboursAfterDestroy(state, level, pos)

  override def tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource): Unit =
    val be = level.getBlockEntity(pos)
    be match
      case sbe: ContainerStasisStorageBlockEntity =>
        sbe.recheckOpen()

object StasisStorageBlock:
  val shelfCodec: MapCodec[StasisShelfBlock] = BlockBehaviour.simpleCodec(StasisShelfBlock.apply)

  class StasisShelfBlock(props: BlockBehaviour.Properties) extends StasisStorageBlock(9, props):
    override def newBlockEntity(worldPosition: BlockPos, blockState: BlockState): BlockEntity =
      new StasisStorageBlockEntity.StasisShelfBlockEntity(worldPosition, blockState)

    override def codec(): MapCodec[? <: BaseEntityBlock] = shelfCodec
