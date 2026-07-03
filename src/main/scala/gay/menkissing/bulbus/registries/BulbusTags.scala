package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object BulbusTags:
  private def tag[T](reg: ResourceKey[Registry[T]], name: String): TagKey[T] =
    TagKey.create(reg, BulbusMod.locate(name))
  
  object item:
    private def tag(name: String): TagKey[Item] = BulbusTags.tag(Registries.ITEM, name)

    val tubeBlacklist: TagKey[Item] = tag("tube_blacklist")
    
    val validToolTag: TagKey[Item] = tag("valid_tools")
    
    
