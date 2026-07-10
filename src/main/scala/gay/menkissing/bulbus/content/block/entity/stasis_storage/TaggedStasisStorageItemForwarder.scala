package gay.menkissing.bulbus.content.block.entity.stasis_storage

import com.mojang.serialization.Codec

/**
 * Like [[StasisStorageItemForwarder]] but it can also save and load data from the world.
 *
 * @tparam Item The storage for each item
 * @tparam Data the data type to load/save
 */

trait TaggedStasisStorageItemForwarder[Item, World, GenericWorld, Data] extends StasisStorageItemForwarder[Item, World, GenericWorld]:
  def dataCodec: Codec[Data]
  
  def loadData(storage: Item, data: Data): Unit
  
  def constructData(storage: Item): Option[Data]

  /**
   * What data will be reset to before reloading.
   * @return
   */
  def defaultData: Data
