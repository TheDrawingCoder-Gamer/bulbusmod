package gay.menkissing.bulbus.util.resources

import gay.menkissing.bulbus.registries.BulbusBlocks
import gay.menkissing.bulbus.registries.BulbusBlockIds
import gay.menkissing.bulbus.registries.BulbusItemIds
import gay.menkissing.bulbus.registries.BulbusBlockIds.BlockIdentifier.descriptionId

trait HasDescriptionId[T] {
  def getDescriptionId(self: T): String
}

object HasDescriptionId:
  given HasDescriptionId[BulbusBlockIds.BlockIdentifier] = _.descriptionId
  given HasDescriptionId[BulbusItemIds.ItemIdentifier] = _.descriptionId