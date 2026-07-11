package gay.menkissing.bulbus.client.content.itemmodels

import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty
import com.mojang.serialization.MapCodec
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.ItemStack
import gay.menkissing.bulbus.content.item.KnowledgeStorageItem
import gay.menkissing.bulbus.registries.BulbusDataComponentTypes

object KnowledgeGemFillRange extends RangeSelectItemModelProperty:
  override val `type`: MapCodec[KnowledgeGemFillRange.type] = MapCodec.unit(this)

  override def get(itemStack: ItemStack, level: ClientLevel | Null, owner: ItemOwner | Null, seed: Int): Float =
    if level != null then
      val comp = itemStack.getOrDefault(BulbusDataComponentTypes.KNOWLEDGE_STORAGE_CONTENTS, 0L)
      if comp > 1_000_000 then
        1.0f
      else
        val max = KnowledgeStorageItem.getMax(level.registryAccess(), itemStack)
        (comp.toDouble / max.toDouble).toFloat
    else
      0.0f


