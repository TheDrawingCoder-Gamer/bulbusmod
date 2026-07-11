package gay.menkissing.bulbus.client

import gay.menkissing.bulbus.client.content.renderers.StasisAccessorBlockEntityRenderer
import gay.menkissing.bulbus.client.content.{BulbusItemModels, BulbusTintSources, TooltipProviders}
import gay.menkissing.bulbus.client.gui.BulbusGuis
import gay.menkissing.bulbus.registries.BulbusBlockEntities
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import gay.menkissing.bulbus.client.content.BulbusBlockEntityRenderers

class BulbusModClient extends ClientModInitializer:
  override def onInitializeClient(): Unit =
    BulbusBlockEntityRenderers.register()
    TooltipProviders.register()
    BulbusTintSources.register()
    BulbusItemModels.register()
    BulbusGuis.register()
    
  