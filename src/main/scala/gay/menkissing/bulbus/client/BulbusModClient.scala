package gay.menkissing.bulbus.client

import gay.menkissing.bulbus.client.content.{BulbusItemModels, BulbusTintSources, TooltipProviders}
import gay.menkissing.bulbus.client.gui.BulbusGuis
import net.fabricmc.api.{ClientModInitializer, EnvType, Environment}

@Environment(EnvType.CLIENT)
class BulbusModClient extends ClientModInitializer:
  override def onInitializeClient(): Unit =
    TooltipProviders.register()
    BulbusTintSources.register()
    BulbusItemModels.register()
    BulbusGuis.register()
  