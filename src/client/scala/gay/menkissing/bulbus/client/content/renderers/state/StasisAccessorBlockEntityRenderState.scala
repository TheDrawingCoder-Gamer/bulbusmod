package gay.menkissing.bulbus.client.content.renderers.state

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.item.ItemStackRenderState

final class StasisAccessorBlockEntityRenderState extends BlockEntityRenderState:
  var storedItem: Option[ItemStackRenderState] = None
  var ageInTicks: Float = 0.0f

  def clear(): Unit =
    storedItem = None
    ageInTicks = 0.0f

