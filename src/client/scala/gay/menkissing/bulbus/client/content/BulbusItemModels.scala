package gay.menkissing.bulbus.client.content

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.client.content.itemmodels.{BottleFluidContentsModel, TubeStoredItemSpecialRenderer}
import net.minecraft.client.renderer.item.ItemModels
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties
import itemmodels.KnowledgeGemFillRange

object BulbusItemModels:
  def register(): Unit =
    ItemModels.ID_MAPPER.put(BulbusMod.locate("stasis_tube/stored"), TubeStoredItemSpecialRenderer.Unbaked.MAP_CODEC)
    ItemModels.ID_MAPPER.put(BulbusMod.locate("stasis_bottle/stored"), BottleFluidContentsModel.Unbaked.MAP_CODEC)
    
    RangeSelectItemModelProperties.ID_MAPPER.put(BulbusMod.locate("knowledge_storage_fill"), KnowledgeGemFillRange.`type`)
