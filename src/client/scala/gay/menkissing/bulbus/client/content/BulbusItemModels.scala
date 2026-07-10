package gay.menkissing.bulbus.client.content

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.client.content.itemmodels.{BottleFluidContentsModel, TubeStoredItemSpecialRenderer}
import net.minecraft.client.renderer.item.ItemModels

object BulbusItemModels:
  def register(): Unit =
    ItemModels.ID_MAPPER.put(BulbusMod.locate("stasis_tube/stored"), TubeStoredItemSpecialRenderer.Unbaked.MAP_CODEC)
    ItemModels.ID_MAPPER.put(BulbusMod.locate("stasis_bottle/stored"), BottleFluidContentsModel.Unbaked.MAP_CODEC)
    
