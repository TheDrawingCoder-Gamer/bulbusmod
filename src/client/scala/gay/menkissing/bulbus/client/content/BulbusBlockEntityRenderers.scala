package gay.menkissing.bulbus.client.content

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import gay.menkissing.bulbus.registries.BulbusBlockEntities
import gay.menkissing.bulbus.client.content.renderers.{
  RepairMachineRenderer,
  StasisAccessorBlockEntityRenderer,
  TunableRenderer
}

object BulbusBlockEntityRenderers:
  def register(): Unit =
    BlockEntityRenderers.register(
      BulbusBlockEntities.stasisAccessor,
      StasisAccessorBlockEntityRenderer.apply
    )
    BlockEntityRenderers
      .register(BulbusBlockEntities.repairMachine, RepairMachineRenderer.apply)
    BlockEntityRenderers
      .register(BulbusBlockEntities.tunableChest, TunableRenderer.apply)
    BlockEntityRenderers
      .register(BulbusBlockEntities.tunableTank, TunableRenderer.apply)
