package gay.menkissing.bulbus.content.block

import com.mojang.serialization.MapCodec
import gay.menkissing.bulbus.content.block.entity.{StasisAccessorBlockEntity, StasisStorageBlockEntity}
import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusSounds}
import net.minecraft.core.BlockPos
import net.minecraft.sounds.{SoundEvent, SoundSource}
import net.minecraft.world.{Containers, InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{BaseEntityBlock, Block}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.phys.BlockHitResult
import org.slf4j.{Logger, LoggerFactory}

class StasisAccessorBlock(props: BlockBehaviour.Properties) extends BaseEntityBlock(props):
  override def codec(): MapCodec[? <: StasisAccessorBlock] = StasisAccessorBlock.accessorCodec

  def quickPlaySound(level: Level, pos: BlockPos, event: SoundEvent): Unit =
    level.playSound(null, pos, event, SoundSource.BLOCKS)

  def playInsertionSound(level: Level, pos: BlockPos): Unit =
    quickPlaySound(level, pos, BulbusSounds.stasisAccessorAddItem)

  def playExtractionSound(level: Level, pos: BlockPos): Unit =
    quickPlaySound(level, pos, BulbusSounds.stasisAccessorRemoveItem)


  def giveOrDrop(level: Level, pos: BlockPos, player: Player, stack: ItemStack): Unit =
    if !player.addItem(stack) then
      Containers.dropItemStack(level, pos.getX, pos.getY + 1.2f, pos.getZ, stack)

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
            playExtractionSound(level, pos)
            giveOrDrop(level, pos, player, result)

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
          if StasisStorageBlockEntity.StorageTests.isAccepted(itemStack) then
            val swappedWith =
              if !bse.getStoredItem.isEmpty then
                bse.containerView.removeItem(0, bse.getStoredItem.getCount)
              else
                ItemStack.EMPTY
            bse.containerView.setItem(0, itemStack.copyWithCount(1))
            playInsertionSound(level, pos)
            bse.setChanged()
            itemStack.shrink(1)
            if !swappedWith.isEmpty then
              giveOrDrop(level, pos, player, swappedWith)
            InteractionResult.SUCCESS
          else
            quickPlaySound(level, pos, BulbusSounds.stasisAccessorAddItemFail)
            InteractionResult.FAIL
        case _ => InteractionResult.PASS

  override def getTicker[T <: BlockEntity](level: Level, blockState: BlockState, `type`: BlockEntityType[T]): BlockEntityTicker[T] | Null =
    if level.isClientSide then
      BaseEntityBlock.createTickerHelper(`type`, BulbusBlockEntities.stasisAccessor, StasisAccessorBlockEntity.ClientTicker)
    else
      null

object StasisAccessorBlock:
  val logger: Logger = LoggerFactory.getLogger(classOf[StasisAccessorBlock])

  val accessorCodec: MapCodec[StasisAccessorBlock] = BlockBehaviour.simpleCodec(StasisAccessorBlock.apply)