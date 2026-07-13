package gay.menkissing.bulbus.content.block

import net.minecraft.world.level.block.Block
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

trait HorizontalPlacementBlock extends Block:
  override def getStateForPlacement(context: BlockPlaceContext): BlockState =
    super.getStateForPlacement(context).setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
