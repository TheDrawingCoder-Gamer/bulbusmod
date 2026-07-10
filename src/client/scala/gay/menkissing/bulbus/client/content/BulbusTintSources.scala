package gay.menkissing.bulbus.client.content

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.client.content.color.item.BottleContentsTint
import net.minecraft.client.color.item.{ItemTintSource, ItemTintSources}

object BulbusTintSources:
  def register(): Unit =
    ItemTintSources.ID_MAPPER.put(BulbusMod.locate("bottle_contents"), BottleContentsTint.MAP_CODEC)
