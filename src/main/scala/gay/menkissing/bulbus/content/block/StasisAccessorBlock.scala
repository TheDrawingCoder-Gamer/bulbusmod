package gay.menkissing.bulbus.content.block

import com.mojang.serialization.MapCodec
import gay.menkissing.bulbus.content.block.entity.{StasisAccessorBlockEntity, StasisStorageBlockEntity}
import net.minecraft.core.BlockPos
import net.minecraft.world.{Containers, InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{BaseEntityBlock, Block}
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.phys.BlockHitResult

class StasisAccessorBlock(props: BlockBehaviour.Properties) extends BaseEntityBlock(props):
  override def codec(): MapCodec[? <: StasisAccessorBlock] = StasisAccessorBlock.accessorCodec

  override def newBlockEntity(worldPosition: BlockPos, blockState: BlockState): BlockEntity =
    new StasisAccessorBlockEntity(worldPosition, blockState)

  override def useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult =
    if level.isClientSide then
      InteractionResult.SUCCESS
    else
      val blockEntity = level.getBlockEntity(pos)
      blockEntity match
        case bse: StasisAccessorBlockEntity =>
          if !bse.containerView.getItem(0).isEmpty then
            val result = bse.containerView.removeItem(0, 1)
            if !player.addItem(result) then
              Containers.dropItemStack(level, pos.getX, pos.getY + 1.2f, pos.getZ, result)

            InteractionResult.SUCCESS
          else
            InteractionResult.FAIL
        case _ => InteractionResult.PASS

  override def useItemOn(itemStack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): InteractionResult =
    if itemStack.isEmpty then
      InteractionResult.TRY_WITH_EMPTY_HAND
    else if level.isClientSide then
      InteractionResult.SUCCESS
    else
      val blockEntity = level.getBlockEntity(pos)
      blockEntity match
        case bse: StasisAccessorBlockEntity =>
          if bse.containerView.getItem(0).isEmpty && StasisStorageBlockEntity.StorageTests.isAccepted(itemStack) then
            bse.containerView.setItem(0, itemStack.copyWithCount(1))
            itemStack.shrink(1)
            InteractionResult.SUCCESS
          else
            InteractionResult.FAIL
        case _ => InteractionResult.PASS

object StasisAccessorBlock:

  val accessorCodec: MapCodec[StasisAccessorBlock] = BlockBehaviour.simpleCodec(StasisAccessorBlock.apply)