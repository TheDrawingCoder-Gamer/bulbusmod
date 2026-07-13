package gay.menkissing.bulbus.content.block

import com.mojang.serialization.MapCodec
import gay.menkissing.bulbus.content.block.entity.RepairMachineBlockEntity
import gay.menkissing.bulbus.registries.BulbusBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{BaseEntityBlock, Block}
import net.minecraft.world.level.block.entity.{
  BlockEntity,
  BlockEntityTicker,
  BlockEntityType
}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.player.Player
import gay.menkissing.bulbus.registries.BulbusItems

import scala.jdk.OptionConverters.*
import net.minecraft.core.component.DataComponents
import gay.menkissing.bulbus.util.BulbusUtil
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext

final class RepairMachineBlock
  (props: BlockBehaviour.Properties)
    extends BaseEntityBlock(props), HorizontalPlacementBlock:
  locally:
    this.registerDefaultState:
      this.stateDefinition.any()
        .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)

  override protected def createBlockStateDefinition
    (builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(BlockStateProperties.HORIZONTAL_FACING)
  override def newBlockEntity
    (worldPosition: BlockPos, blockState: BlockState): BlockEntity =
    new RepairMachineBlockEntity(worldPosition, blockState)

  override def codec(): MapCodec[? <: BaseEntityBlock] =
    RepairMachineBlock.codec

  override def getTicker[T <: BlockEntity]
    (
      level: Level,
      blockState: BlockState,
      `type`: BlockEntityType[T]
    ): BlockEntityTicker[T] | Null =
    if !level.isClientSide then
      BaseEntityBlock.createTickerHelper(
        `type`,
        BulbusBlockEntities.repairMachine,
        RepairMachineBlockEntity.ServerTicker
      )
    else
      BaseEntityBlock.createTickerHelper(
        `type`,
        BulbusBlockEntities.repairMachine,
        RepairMachineBlockEntity.ClientTicker
      )
  override def useWithoutItem
    (
      state: BlockState,
      level: Level,
      pos: BlockPos,
      player: Player,
      hitResult: BlockHitResult
    ): InteractionResult =
    if level.isClientSide then InteractionResult.SUCCESS
    else
      level.getBlockEntity(pos, BulbusBlockEntities.repairMachine).toScala match
        case Some(be) =>
          if !be.primaryItem.isEmpty then
            val newStack = be.primaryItem
            be.primaryItem = ItemStack.EMPTY
            BulbusUtil.giveOrDrop(level, pos, player, newStack)
            InteractionResult.SUCCESS
          else if !be.heldGem.isEmpty then
            val newStack = be.heldGem
            be.heldGem = ItemStack.EMPTY
            BulbusUtil.giveOrDrop(level, pos, player, newStack)
            InteractionResult.SUCCESS
          else InteractionResult.PASS
        case _ => InteractionResult.PASS

  override def useItemOn
    (
      stack: ItemStack,
      state: BlockState,
      level: Level,
      pos: BlockPos,
      player: Player,
      hand: InteractionHand,
      hitResult: BlockHitResult
    ): InteractionResult =
    if stack.isEmpty then InteractionResult.TRY_WITH_EMPTY_HAND
    else if level.isClientSide then InteractionResult.SUCCESS
    else
      level.getBlockEntity(pos, BulbusBlockEntities.repairMachine).toScala match
        case Some(be) =>
          if stack.is(BulbusItems.knowledgeStorage) then
            if be.heldGem.isEmpty then
              be.heldGem = stack.copyAndClear()
              // todo: sound
              InteractionResult.SUCCESS
            else InteractionResult.CONSUME
          else if stack.has(DataComponents.DAMAGE) then
            if be.primaryItem.isEmpty then
              be.primaryItem = stack.copyAndClear()
              // todo: sound
              InteractionResult.SUCCESS
            else InteractionResult.CONSUME
          else InteractionResult.PASS

        case None => InteractionResult.PASS

object RepairMachineBlock:
  val codec: MapCodec[RepairMachineBlock] =
    BlockBehaviour.simpleCodec(RepairMachineBlock.apply)
