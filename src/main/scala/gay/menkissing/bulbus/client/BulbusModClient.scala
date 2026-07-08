package gay.menkissing.bulbus.client

import gay.menkissing.bulbus.client.content.{BulbusItemModels, BulbusTintSources, TooltipProviders}
import gay.menkissing.bulbus.client.gui.BulbusGuis
import gay.menkissing.bulbus.content.block.entity.StasisAccessorBlockEntity
import gay.menkissing.bulbus.registries.BulbusBlockEntities
import net.fabricmc.api.{ClientModInitializer, EnvType, Environment}
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers

@Environment(EnvType.CLIENT)
class BulbusModClient extends ClientModInitializer:
  override def onInitializeClient(): Unit =
    BlockEntityRenderers.register(BulbusBlockEntities.stasisAccessor, StasisAccessorBlockEntity.StasisAccessorBlockEntityRenderer.apply)
    TooltipProviders.register()
    BulbusTintSources.register()
    BulbusItemModels.register()
    BulbusGuis.register()
    
  