package gay.menkissing.bulbus.infra.lookup

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.content.item.{StasisBottleItem, StasisTubeItem}
import gay.menkissing.bulbus.registries.{BulbusDataComponentTypes, BulbusItems}
import gay.menkissing.bulbus.util.storage.StorageSlotinator
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidStorage, FluidVariant}
import net.fabricmc.fabric.api.transfer.v1.item.{ItemStorage, ItemVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, StoragePreconditions, StorageView, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.core.component.{DataComponentPatch, DataComponentType}

import java.{lang, util}

trait StasisStorage[T] extends SlottedStorage[T]:
  def getFilter: T
  def setFilter(value: T): Unit
  def unsetFilter(): Unit


object StasisStorage:
  val item: ItemApiLookup[StasisStorage[ItemVariant], ContainerItemContext] =
    ItemApiLookup.get(BulbusMod.locate("stasis_item_storage"), classOf[StasisStorage[ItemVariant]], classOf[ContainerItemContext])

  val fluid: ItemApiLookup[StasisStorage[FluidVariant], ContainerItemContext] =
    ItemApiLookup.get(BulbusMod.locate("stasis_fluid_storage"), classOf[StasisStorage[FluidVariant]], classOf[ContainerItemContext])

  def init(): Unit =
    item.registerFallback: (item, ctx) =>
      ItemStorage.ITEM.find(item, ctx) match
        case null => null
        case slotted: SlottedStorage[ItemVariant] => SlottedStorageWrapper(slotted, ItemVariant.blank())
        case unslotted =>
          val evil = StorageSlotinator(unslotted)
          SlottedStorageWrapper(evil, ItemVariant.blank())
        
        
    fluid.registerFallback: (item, ctx) =>
      FluidStorage.ITEM.find(item, ctx) match
        case null => null
        case slotted: SlottedStorage[FluidVariant] => SlottedStorageWrapper(slotted, FluidVariant.blank())
        case unslotted =>
          val evil = StorageSlotinator(unslotted)
          SlottedStorageWrapper(evil, FluidVariant.blank())
          
    item.registerForItems(
      (item, ctx) => StasisTubeStorage(StasisTubeItem.getMaxEvil(item), ctx),
      BulbusItems.stasisTube
    )
    
    fluid.registerForItems(
      (item, ctx) => StasisBottleStorage(StasisBottleItem.getMaxEvil(item), ctx),
      BulbusItems.stasisBottle
    )


  class SlottedStorageWrapper[T](protected val inner: SlottedStorage[T], protected val blankResource: T) extends StasisStorage[T]:
    // > StasisStorage
    override def getFilter: T = blankResource

    override def setFilter(value: T): Unit = ()

    override def unsetFilter(): Unit = ()

    // > SlottedStorage

    override def getSlotCount: Int = inner.getSlotCount

    override def getSlot(slot: Int): SingleSlotStorage[T] = inner.getSlot(slot)

    override def getSlots: util.List[SingleSlotStorage[T]] = inner.getSlots

    // > Storage

    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      inner.insert(resource, maxAmount, transaction)

    override def supportsInsertion(): Boolean = inner.supportsInsertion()

    override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      inner.extract(resource, maxAmount, transaction)

    override def supportsExtraction(): Boolean =
      inner.supportsExtraction()

    override def iterator(): util.Iterator[StorageView[T]] =
      inner.iterator()

    override def nonEmptyIterator(): util.Iterator[StorageView[T]] =
      inner.nonEmptyIterator()

    override def nonEmptyViews(): lang.Iterable[StorageView[T]] =
      inner.nonEmptyViews()

    override def getVersion: Long =
      inner.getVersion

  trait TransferStasisStorageMixin[T] extends StasisStorage[T]:
    protected var filter: T = getBlankResource

    override def getFilter: T = filter

    override def setFilter(value: T): Unit = filter = value

    override def unsetFilter(): Unit = filter = getBlankResource


    protected def getBlankResource: T

  abstract class FilterableStorage[T <: TransferVariant[?]](val capacity: Long, val context: ContainerItemContext)
    extends SingleSlotStorage[T], StasisStorage[T], TransferStasisStorageMixin[T]:

    protected val contentsType: DataComponentType[StorageItemContents[T]]

    protected def defaultContents: StorageItemContents[T]

    protected def contents: StorageItemContents[T] =
      context.getItemVariant.getOrDefault(contentsType, defaultContents)

    protected def applyContents(contents: StorageItemContents[T], transaction: TransactionContext): Unit =
      // ???
      if !context.getItemVariant.isBlank then
        val newVariant = context.getItemVariant.withComponents(DataComponentPatch.builder().set(contentsType, contents).build())
        context.exchange(newVariant, 1, transaction)

    override def getCapacity: Long = capacity

    override def getResource: T =
      contents.variant

    override def isResourceBlank: Boolean = getResource.isBlank

    override def getAmount: Long = contents.amount

    private def validVariant(storedVariant: T, resource: T): Boolean =
      if !storedVariant.isBlank then
        filter = storedVariant

      filter.isBlank || filter == resource

    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      val contents = this.contents

      if validVariant(contents.variant, resource) then
        val builder = StorageItemContents.Builder(contents.variant, contents.amount, capacity, getBlankResource)

        val inserted = builder.insert(resource, maxAmount)
        applyContents(builder.result, transaction)

        inserted
      else
        0L

    override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      val contents = this.contents

      if validVariant(contents.variant, resource) then
        val builder = StorageItemContents.Builder(contents.variant, contents.amount, capacity, getBlankResource)

        val extracted = builder.extract(resource, maxAmount)
        applyContents(builder.result, transaction)

        extracted
      else
        0L

  final class StasisTubeStorage(capacity: Long, context: ContainerItemContext) extends FilterableStorage[ItemVariant](capacity, context):
    override val contentsType: DataComponentType[StorageItemContents[ItemVariant]] = BulbusDataComponentTypes.STASIS_TUBE_CONTENTS

    override def defaultContents: StorageItemContents[ItemVariant] = StorageItemContents.Item.DEFAULT

    override def getBlankResource: ItemVariant = ItemVariant.blank()

  final class StasisBottleStorage(capacity: Long, context: ContainerItemContext) extends FilterableStorage[FluidVariant](capacity, context):
    override val contentsType: DataComponentType[StorageItemContents[FluidVariant]] = BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS

    override def defaultContents: StorageItemContents[FluidVariant] = StorageItemContents.Fluid.DEFAULT

    override def getBlankResource: FluidVariant = FluidVariant.blank()

