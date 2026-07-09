package gay.menkissing.bulbus.content.block.entity.stasis_storage

import com.mojang.serialization.Codec

/**
 * Like [[StasisStorageItemForwarder]] but it can also save and load data from the world.
 *
 * @tparam T the storage that will be saved and exposed to the world
 * @tparam D the data type to load/save
 */

trait TaggedStasisStorageItemForwarder[T, S, D] extends StasisStorageItemForwarder[T, S]:  
  def dataCodec: Codec[D]
  
  def loadData(storage: T, data: D): Unit
  
  def constructData(storage: T): Option[D]

  /**
   * What data will be reset to before reloading.
   * @return
   */
  def defaultData: D
