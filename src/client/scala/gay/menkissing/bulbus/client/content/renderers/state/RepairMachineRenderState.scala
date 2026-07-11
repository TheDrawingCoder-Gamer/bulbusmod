package gay.menkissing.bulbus.client.content.renderers.state

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.item.ItemStackRenderState
import com.mojang.math.Quadrant

class RepairMachineRenderState extends BlockEntityRenderState:
  var gem: Option[ItemStackRenderState] = None
  var primaryItem: Option[ItemStackRenderState] = None
  var ageInTicks: Float = 0.0f
  var active: Boolean = false
  var rotation: Float = 0.0f
