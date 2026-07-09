package gay.menkissing.bulbus.infra.lookup.base

import gay.menkissing.bulbus.infra.lookup.SingleTypeStorageLike
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.NonNullList
import team.reborn.energy.api.EnergyStorage

class BulbusCombinedEnergyStorage(parts: NonNullList[EnergyStorage])
  extends EnergyStorage, CombinedSingleTypeStorageLike[EnergyStorage](parts):
  override def instance: SingleTypeStorageLike[EnergyStorage] = SingleTypeStorageLike.forEnergyStorage

  override def supportsInsertion(): Boolean =
    parts.stream().anyMatch(_.supportsInsertion())

  override def supportsExtraction(): Boolean =
    parts.stream().anyMatch(_.supportsExtraction())


