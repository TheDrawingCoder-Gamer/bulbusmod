package gay.menkissing.bulbus.client.content.renderers.state

import gay.menkissing.bulbus.persistent.TuningChannel
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState

final class TunableRenderState extends BlockEntityRenderState:
  var channel: TuningChannel = TuningChannel.DEFAULT
  var rotation: Float = 0.0f
