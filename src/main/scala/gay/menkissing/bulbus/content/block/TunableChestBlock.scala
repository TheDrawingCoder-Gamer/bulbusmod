package gay.menkissing.bulbus.content.block

import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import gay.menkissing.bulbus.content.block.entity.TunableChestBlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.Level
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.CubeVoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import net.minecraft.core.component.DataComponents

final class TunableChestBlock(props: BlockBehaviour.Properties) extends BaseEntityBlock(props):
  override protected def codec(): MapCodec[? <: BaseEntityBlock] = TunableChestBlock.CODEC


  override protected def getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
    Shapes.or(
      Shapes.block(),
      TunableChestBlock.btn1Shape,
      TunableChestBlock.btn2Shape,
      TunableChestBlock.btn3Shape
    )

  override def newBlockEntity(worldPosition: BlockPos, blockState: BlockState): BlockEntity =
    new TunableChestBlockEntity(worldPosition, blockState)

  override protected def useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult =
    if level.isClientSide then
      InteractionResult.SUCCESS
    else
      val blockEntity = level.getBlockEntity(pos)
      blockEntity match
        case be: TunableChestBlockEntity =>
          player.openMenu(be.getContainer)
        case _ => ()
      InteractionResult.CONSUME
  
  override protected def useItemOn(itemStack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): InteractionResult =
    if level.isClientSide then
      InteractionResult.TRY_WITH_EMPTY_HAND
    else if itemStack.has(DataComponents.DYE) then
      val relativePos: Vec3 = hitResult.getLocation().subtract(pos.getX, pos.getY, pos.getZ)
      val toTryDye =
        if TunableChestBlock.inButton(0, relativePos) then
          0
        else if TunableChestBlock.inButton(1, relativePos) then
          1
        else if TunableChestBlock.inButton(2, relativePos) then
          2
        else -1
      if toTryDye != -1 then
        val dye = itemStack.get(DataComponents.DYE)
        level.getBlockEntity(pos) match
          case be: TunableChestBlockEntity =>
            if !player.getAbilities.instabuild then
              itemStack.shrink(1)
            toTryDye match
              case 0 =>
                be.channel = be.channel.copy(first = dye)
              case 1 =>
                be.channel = be.channel.copy(second = dye)
              case 2 =>
                be.channel = be.channel.copy(third = dye)
              case _ => ()
            
            InteractionResult.SUCCESS
          case _ => InteractionResult.TRY_WITH_EMPTY_HAND
          

      else
        InteractionResult.TRY_WITH_EMPTY_HAND
    else
      InteractionResult.TRY_WITH_EMPTY_HAND

object TunableChestBlock:
  val CODEC: MapCodec[TunableChestBlock] = BlockBehaviour.simpleCodec(TunableChestBlock.apply)

  val btn1Shape: VoxelShape = button(4)
  val btn2Shape: VoxelShape = button(7)
  val btn3Shape: VoxelShape = button(10)

  def buttonX(idx: Int): Int =
    4 + idx * 3

  def inButton(idx: Int, pos: Vec3): Boolean =
    val xPx = buttonX(idx)
    pos.y >= (15.0 / 16.0) && pos.y <= (17.0 / 16.0)
      && pos.z >= (6.0 / 16) && pos.z <= (10.0 / 16)
      && pos.x >= (xPx / 16.0) && pos.x <= ((xPx + 2.0) / 16.0)

  def button(xPx: Int): VoxelShape =
    button(xPx, 15, 6)

  def button(minXPx: Int, minYPx: Int, minZPx: Int): VoxelShape =
    val px2 = 2.0 / 16
    val px4 = 4.0 / 16

    val minX: Double = minXPx.toDouble / 16
    val minY: Double = minYPx.toDouble / 16
    val minZ: Double = minZPx.toDouble / 16
    Shapes.box(minX, minY, minZ, minX + px2, minY + px2, minZ + px4)