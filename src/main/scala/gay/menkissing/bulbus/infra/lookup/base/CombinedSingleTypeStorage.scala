package gay.menkissing.bulbus.infra.lookup.base

import gay.menkissing.bulbus.api.SingleTypeStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.NonNullList

class CombinedSingleTypeStorage(val parts: NonNullList[SingleTypeStorage])
  extends SingleTypeStorage:

  override def supportsInsertion(): Boolean =
    parts.stream().anyMatch(_.supportsInsertion())

  override def supportsExtraction(): Boolean =
    parts.stream().anyMatch(_.supportsExtraction())

  override def insert(maxAmount: Long, transaction: TransactionContext): Long =
    StoragePreconditions.notNegative(maxAmount)
    var amount: Long = 0
    
    var i = 0
    while i < parts.size() do
      val part = parts.get(i)
      amount += part.insert(maxAmount - amount, transaction)
      if amount == maxAmount then return amount
      
      i += 1
    
    amount

  override def extract(maxAmount: Long, transaction: TransactionContext): Long =
    StoragePreconditions.notNegative(maxAmount)
    var amount: Long = 0
    
    var i = 0
    while i < parts.size() do
      val part = parts.get(i)
      amount += part.extract(maxAmount - amount, transaction)
      if amount == maxAmount then return amount
      
      i += 1
    
    amount

  override def getAmount: Long = parts.stream().mapToLong(_.getAmount).sum()

  override def getCapacity: Long = parts.stream().mapToLong(_.getCapacity).sum()
  
