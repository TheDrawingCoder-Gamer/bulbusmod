package gay.menkissing.bulbus.infra.lookup.base

import gay.menkissing.bulbus.infra.lookup.SingleTypeStorageLike
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.NonNullList

trait CombinedSingleTypeStorageLike[T](val parts: NonNullList[T]):
  def instance: SingleTypeStorageLike[T]

  // TODO: can't override supports insertion/extraction

  def insert(maxAmount: Long, transaction: TransactionContext): Long =
    StoragePreconditions.notNegative(maxAmount)
    var amount: Long = 0

    var i = 0
    while i < parts.size() do
      val part = parts.get(i)
      amount += instance.insert(part)(maxAmount - amount, transaction)
      if amount == maxAmount then return amount

      i += 1

    amount

  def extract(maxAmount: Long, transaction: TransactionContext): Long =
    StoragePreconditions.notNegative(maxAmount)
    var amount: Long = 0

    var i = 0
    while i < parts.size() do
      val part = parts.get(i)
      amount += instance.extract(part)(maxAmount - amount, transaction)
      if amount == maxAmount then return amount

      i += 1

    amount

  def getAmount: Long = parts.stream().mapToLong(instance.getAmount).sum()

  def getCapacity: Long = parts.stream().mapToLong(instance.getCapacity).sum()

