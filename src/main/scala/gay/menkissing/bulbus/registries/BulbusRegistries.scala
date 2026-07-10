package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.content.block.entity.stasis_storage.StasisStorageItemForwarder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object BulbusRegistries:
  val itemForwarder: ResourceKey[Registry[StasisStorageItemForwarder[?, ?, ?]]] = ResourceKey
    .createRegistryKey(BulbusMod.locate("stasis_storage_item_forwarder"))
  

