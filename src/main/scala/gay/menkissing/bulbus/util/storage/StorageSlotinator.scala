package gay.menkissing.bulbus.util.storage

import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, Storage, StorageView, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext

import java.util
import scala.jdk.CollectionConverters.*

class StorageSlotinator[T](protected val inner: Storage[T]) extends SlottedStorage[T]:
  // FORWARDERS
  override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
    inner.insert(resource, maxAmount, transaction)

  override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
    inner.extract(resource, maxAmount, transaction)

  override def supportsInsertion(): Boolean = inner.supportsInsertion()

  override def supportsExtraction(): Boolean = inner.supportsExtraction()

  override def iterator(): util.Iterator[StorageView[T]] = inner.iterator()

  // EVIL CODE
  override def getSlotCount: Int = iterator().asScala.size

  override def getSlot(slot: Int): SingleSlotStorage[T] =
    iterator().asScala.drop(slot).nextOption()
              .map(it => StorageSlotinator.SingleSlotinator[T](inner, it))
              .getOrElse(throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds of this wrapped storage"))

object StorageSlotinator:
  class SingleSlotinator[T](protected val inner: Storage[T], protected val view: StorageView[T])
    extends SingleSlotStorage[T]:
    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      inner.insert(resource, maxAmount, transaction)

    override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      view.extract(resource, maxAmount, transaction)

    override def isResourceBlank: Boolean =
      view.isResourceBlank

    override def getResource: T =
      view.getResource

    override def getAmount: Long =
      view.getAmount

    override def getCapacity: Long =
      view.getCapacity