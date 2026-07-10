package gay.menkissing.bulbus.registries

import com.mojang.serialization.Lifecycle
import gay.menkissing.bulbus.content.block.entity.stasis_storage.StasisStorageItemForwarder
import net.minecraft.core.{MappedRegistry, Registry}

object BulbusBuiltInRegistries:
  val itemForwarder: Registry[StasisStorageItemForwarder[?, ?, ?]] = new MappedRegistry[StasisStorageItemForwarder[?, ?, ?]](BulbusRegistries.itemForwarder, Lifecycle
    .stable())

  def init(): Unit = ()