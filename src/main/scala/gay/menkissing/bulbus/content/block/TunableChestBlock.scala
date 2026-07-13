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
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.StateDefinition.Builder
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.Direction
import org.joml.Vector2i

import gay.menkissing.bulbus.util.BulbusConstants
import com.mojang.math.Axis
import org.joml.Vector3f
import com.mojang.math.OctahedralGroup

final class TunableChestBlock
  (props: BlockBehaviour.Properties)
    extends BaseEntityBlock(props),
      HorizontalPlacementBlock:
  locally:
    this.registerDefaultState:
      this.stateDefinition.any()
        .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)

  override protected def codec(): MapCodec[? <: BaseEntityBlock] =
    TunableChestBlock.CODEC

  override protected def createBlockStateDefinition
    (builder: Builder[Block, BlockState]): Unit =
    builder.add(BlockStateProperties.HORIZONTAL_FACING)

  override protected def getShape
    (
      state: BlockState,
      level: BlockGetter,
      pos: BlockPos,
      context: CollisionContext
    ): VoxelShape =
    val dir = 
      state.getValue(BlockStateProperties.HORIZONTAL_FACING) match
        case Direction.NORTH => 0
        case Direction.UP => 0
        case Direction.DOWN => 0
        case Direction.SOUTH => 2
        case Direction.WEST => 3
        case Direction.EAST => 1
      
    TunableChestBlock.shapes(dir)

  override def newBlockEntity
    (worldPosition: BlockPos, blockState: BlockState): BlockEntity =
    new TunableChestBlockEntity(worldPosition, blockState)

  override protected def useWithoutItem
    (
      state: BlockState,
      level: Level,
      pos: BlockPos,
      player: Player,
      hitResult: BlockHitResult
    ): InteractionResult =
    if level.isClientSide then InteractionResult.SUCCESS
    else
      val blockEntity = level.getBlockEntity(pos)
      blockEntity match
        case be: TunableChestBlockEntity => player.openMenu(be.getContainer)
        case _                           => ()
      InteractionResult.CONSUME

  override protected def useItemOn
    (
      itemStack: ItemStack,
      state: BlockState,
      level: Level,
      pos: BlockPos,
      player: Player,
      hand: InteractionHand,
      hitResult: BlockHitResult
    ): InteractionResult =
    if level.isClientSide then InteractionResult.TRY_WITH_EMPTY_HAND
    else if itemStack.has(DataComponents.DYE) then
      val relativePos: Vec3 =
        hitResult.getLocation().subtract(pos.getX, pos.getY, pos.getZ)
      val dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING)
      val dirIdx = dir match
        case Direction.NORTH => 0
        case Direction.UP => 0
        case Direction.DOWN => 0
        case Direction.SOUTH => 2
        // swapin it crazy style
        case Direction.WEST => 1
        case Direction.EAST => 3

      val btnShapes = TunableChestBlock.buttons(dirIdx)
      
      val toTryDye =
        if TunableChestBlock.isInside(btnShapes.btn1, relativePos) then 0
        else if TunableChestBlock.isInside(btnShapes.btn2, relativePos) then 1
        else if TunableChestBlock.isInside(btnShapes.btn3, relativePos) then 2
        else -1
      if toTryDye != -1 then
        val dye = itemStack.get(DataComponents.DYE)
        level.getBlockEntity(pos) match
          case be: TunableChestBlockEntity =>
            if !player.getAbilities.instabuild then itemStack.shrink(1)
            toTryDye match
              case 0 => be.channel = be.channel.copy(first = dye)
              case 1 => be.channel = be.channel.copy(second = dye)
              case 2 => be.channel = be.channel.copy(third = dye)
              case _ => ()

            InteractionResult.SUCCESS
          case _ => InteractionResult.TRY_WITH_EMPTY_HAND
      else InteractionResult.TRY_WITH_EMPTY_HAND
    else InteractionResult.TRY_WITH_EMPTY_HAND

object TunableChestBlock:
  val CODEC: MapCodec[TunableChestBlock] =
    BlockBehaviour.simpleCodec(TunableChestBlock.apply)

  

  type ButtonShapes = (btn1: VoxelShape, btn2: VoxelShape, btn3: VoxelShape)

  def isInside(shape: VoxelShape, pos: Vec3): Boolean =
    shape.closestPointTo(pos).get() == pos

  val baseButton: VoxelShape = Shapes.box(4 / 16.0, 15 / 16.0, 6 / 16.0, 6 / 16.0, 17 / 16.0, 10 / 16.0)

  val buttons: Vector[ButtonShapes] =
    (0 until 4).map: rot =>
      val rotation =
        rot match
          case 0 => OctahedralGroup.IDENTITY
          case 1 => OctahedralGroup.BLOCK_ROT_Y_90
          case 2 => OctahedralGroup.BLOCK_ROT_Y_180
          case 3 => OctahedralGroup.BLOCK_ROT_Y_270
          case _ => OctahedralGroup.IDENTITY
          
      val btn1 = 
        Shapes.rotate(baseButton, rotation)
      val btn2 =
        Shapes.rotate(baseButton.move(3 / 16.0, 0, 0), rotation)
      val btn3 =
        Shapes.rotate(baseButton.move(6 / 16.0, 0, 0), rotation)
      (btn1, btn2, btn3)
    .toVector

  val shapes: Vector[VoxelShape] =
    (0 until 4).map: idx =>
      val shapes = buttons(idx)
      Shapes.or(
        Shapes.block(),
        shapes.btn1,
        shapes.btn2,
        shapes.btn3
      )
    .toVector



