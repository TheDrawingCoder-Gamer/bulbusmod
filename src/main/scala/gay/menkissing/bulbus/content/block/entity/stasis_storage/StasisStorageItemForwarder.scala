package gay.menkissing.bulbus.content.block.entity.stasis_storage

import gay.menkissing.bulbus.api.SingleTypeStorage
import gay.menkissing.bulbus.content.block.entity.{StasisStorageBlockEntity, StasisWormBlockEntity}
import gay.menkissing.bulbus.infra.lookup.StasisStorage
import gay.menkissing.bulbus.infra.lookup.base.{CombinedSingleTypeStorage, EmptySingleTypeStorage}
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.storage.base.{CombinedSlottedStorage, SingleSlotStorage}
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, Storage, StoragePreconditions, StorageView, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.{Direction, NonNullList}
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

import java.util

/**
 * An item forwarder abstracts over a storage that can be extracted from items
 * and end up being exposed to the world from a stasis storage.
 * @tparam T the storage that will be saved and exposed to the world
 */
trait StasisStorageItemForwarder[T, S]:
  def accepts(stack: ItemStack, dummyCtx: ContainerItemContext): Boolean =
    tryLoadStorage(stack, dummyCtx).isDefined

  /**
   * Try loading this forwarder's storage type from the item.
   * @param stack an Item
   * @param ctx the container context, for use with any ItemApiLookup calls
   * @return Either Some storage type, or None
   */
  def tryLoadStorage(stack: ItemStack, ctx: ContainerItemContext): Option[T]

  /**
   * @return The name of this forwarder
   */
  def name: Identifier

  /**
   * @return A default storage implementation that will effectively be a no op when exposed
   */
  def empty: T

  /**
   * @return Some storage that allows insertion but deletes all items, or None if this forwarder shouldn't support that
   */
  def voiding: Option[T] = None

  /**
   * Create something that can end up being submitted to some lookup api.
   * NOTE: It's not submitted by itself - you'll need to submit it yourself.
   * @param slots A list of the slots storages. When passed, it will all be empty.
   * @return
   */
  def createExposed(slots: NonNullList[T]): S

  /**
   * Make a storage that will forward to the next block.
   * See [[StasisWormBlockEntity.StorageHelper]] for a base trait to use.
   * @param input
   * @param parent
   * @return
   */
  def wrapWorm(input: S, parent: StasisWormBlockEntity): S


object StasisStorageItemForwarder:
  trait SingleTypeForwarder extends StasisStorageItemForwarder[SingleTypeStorage, SingleTypeStorage]:
    def blockLookup: BlockApiLookup[SingleTypeStorage, Direction | Null]
    def itemLookup: ItemApiLookup[SingleTypeStorage, ContainerItemContext]

    override def tryLoadStorage(stack: ItemStack, ctx: ContainerItemContext): Option[SingleTypeStorage] =
      Option(ctx.find(itemLookup))

    override def empty: SingleTypeStorage = EmptySingleTypeStorage

    override def createExposed(slots: NonNullList[SingleTypeStorage]): SingleTypeStorage =
      CombinedSingleTypeStorage(slots)

    override def wrapWorm(input: SingleTypeStorage, parentIn: StasisWormBlockEntity): SingleTypeStorage =
      new SingleTypeStorage with StasisWormBlockEntity.StorageHelper[SingleTypeStorage]:
        override val parent: StasisWormBlockEntity = parentIn
        override def lookup: BlockApiLookup[SingleTypeStorage, Direction | Null] = blockLookup

        override def insert(maxAmount: Long, transaction: TransactionContext): Long =
          StoragePreconditions.notNegative(maxAmount)

          val amount = input.insert(maxAmount, transaction)
          if amount < maxAmount then
            amount + withNext(_.insert(maxAmount - amount, transaction)).getOrElse(0L)
          else
            amount

        override def extract(maxAmount: Long, transaction: TransactionContext): Long =
          StoragePreconditions.notNegative(maxAmount)

          val amount = input.extract(maxAmount, transaction)
          if amount < maxAmount then
            amount + withNext(_.extract(maxAmount - amount, transaction)).getOrElse(0L)
          else
            amount

        override def getAmount: Long =
          input.getAmount + withNext(_.getAmount).getOrElse(0L)

        override def getCapacity: Long =
          input.getCapacity + withNext(_.getCapacity).getOrElse(0L)

  trait StasisStorageForwarderMixin[I <: TransferVariant[?]] extends TaggedStasisStorageItemForwarder[StasisStorage[I], SlottedStorage[I], I]:
    def blockLookup: BlockApiLookup[Storage[I], Direction | Null]
    def itemLookup: ItemApiLookup[StasisStorage[I], ContainerItemContext]
    def blank: I

    override def constructData(storage: StasisStorage[I]): Option[I] =
      Option.when(!storage.getFilter.isBlank)(storage.getFilter)

    override def loadData(storage: StasisStorage[I], data: I): Unit =
      storage.setFilter(data)

    override def defaultData: I = blank

    override def tryLoadStorage(stack: ItemStack, ctx: ContainerItemContext): Option[StasisStorage[I]] =
      Option(ctx.find(itemLookup))

    object VoidingSlot extends StasisStorageBlockEntity.VoidingSlot[I]:
      override def getBlank: I = blank

    object EmptySlot extends StasisStorageBlockEntity.EmptySlot[I]:
      override def getBlank: I = blank

    override def empty: StasisStorage[I] = EmptySlot

    override val voiding: Option[StasisStorage[I]] = Some(VoidingSlot)

    override def createExposed(slots: NonNullList[StasisStorage[I]]): SlottedStorage[I] =
      CombinedSlottedStorage(slots)

    override def wrapWorm(input: SlottedStorage[I], parentIn: StasisWormBlockEntity): SlottedStorage[I] =
      new SlottedStorage[I] with StasisWormBlockEntity.StorageMixin[I]:
        override val parent: StasisWormBlockEntity = parentIn
        override val storages: SlottedStorage[I] = input

        override def lookup: BlockApiLookup[Storage[I], Direction | Null] = blockLookup
