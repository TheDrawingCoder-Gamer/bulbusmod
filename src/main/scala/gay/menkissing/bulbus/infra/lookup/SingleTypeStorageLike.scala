package gay.menkissing.bulbus.infra.lookup

import gay.menkissing.bulbus.api.SingleTypeStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import team.reborn.energy.api.EnergyStorage

trait SingleTypeStorageLike[T]:
  def supportsInsertion(self: T): Boolean
  def supportsExtraction(self: T): Boolean

  def insert(self: T)(maxAmount: Long, transaction: TransactionContext): Long

  def extract(self: T)(maxAmount: Long, transaction: TransactionContext): Long

  def getAmount(self: T): Long

  def getCapacity(self: T): Long

object SingleTypeStorageLike:
  val forSingleTypeStorage: SingleTypeStorageLike[SingleTypeStorage] =
    new SingleTypeStorageLike[SingleTypeStorage]:
      override def supportsInsertion(self: SingleTypeStorage): Boolean = self.supportsInsertion()

      override def supportsExtraction(self: SingleTypeStorage): Boolean = self.supportsExtraction()

      override def insert(self: SingleTypeStorage)(maxAmount: Long, transaction: TransactionContext): Long =
        self.insert(maxAmount, transaction)

      override def extract(self: SingleTypeStorage)(maxAmount: Long, transaction: TransactionContext): Long =
        self.extract(maxAmount, transaction)

      override def getAmount(self: SingleTypeStorage): Long =
        self.getAmount

      override def getCapacity(self: SingleTypeStorage): Long =
        self.getCapacity
  
  val forEnergyStorage: SingleTypeStorageLike[EnergyStorage] =
    new SingleTypeStorageLike[EnergyStorage]:
      override def supportsInsertion(self: EnergyStorage): Boolean = self.supportsInsertion()

      override def supportsExtraction(self: EnergyStorage): Boolean = self.supportsExtraction()

      override def insert(self: EnergyStorage)(maxAmount: Long, transaction: TransactionContext): Long =
        self.insert(maxAmount, transaction)

      override def extract(self: EnergyStorage)(maxAmount: Long, transaction: TransactionContext): Long =
        self.extract(maxAmount, transaction)

      override def getAmount(self: EnergyStorage): Long =
        self.getAmount

      override def getCapacity(self: EnergyStorage): Long =
        self.getCapacity