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
import gay.menkissing.bulbus.content.block.entity.TunableBlockEntity
import gay.menkissing.bulbus.content.block.entity.TunableTankBlockEntity

final class TunableTankBlock
  (props: BlockBehaviour.Properties)
    extends TunableBlock(props):

  override protected def codec(): MapCodec[? <: BaseEntityBlock] =
    TunableTankBlock.CODEC
  override def newBlockEntity
    (worldPosition: BlockPos, blockState: BlockState): BlockEntity =
    new TunableTankBlockEntity(worldPosition, blockState)

object TunableTankBlock:
  val CODEC: MapCodec[TunableTankBlock] =
    BlockBehaviour.simpleCodec(TunableTankBlock.apply)

  

  



