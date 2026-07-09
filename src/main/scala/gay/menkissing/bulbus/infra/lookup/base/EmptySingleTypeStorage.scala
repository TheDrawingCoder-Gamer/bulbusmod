package gay.menkissing.bulbus.infra.lookup.base

import gay.menkissing.bulbus.api.SingleTypeStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext

object EmptySingleTypeStorage extends SingleTypeStorage:
  override def supportsInsertion(): Boolean = false

  override def supportsExtraction(): Boolean = false
  
  override def insert(maxAmount: Long, transaction: TransactionContext): Long = 0
  
  override def extract(maxAmount: Long, transaction: TransactionContext): Long = 0

  override def getAmount: Long = 0

  override def getCapacity: Long = 0
