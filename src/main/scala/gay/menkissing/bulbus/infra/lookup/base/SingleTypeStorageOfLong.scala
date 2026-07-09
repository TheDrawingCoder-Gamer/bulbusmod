package gay.menkissing.bulbus.infra.lookup.base

import gay.menkissing.bulbus.api.SingleTypeStorage
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.component.{DataComponentPatch, DataComponentType}

class SingleTypeStorageOfLong(val kind: DataComponentType[Long], val context: ContainerItemContext, val capacity: Long)
  extends SingleTypeStorage:

  override def getCapacity: Long = capacity

  override def getAmount: Long =
    context.getItemVariant.getOrDefault(kind, 0L)
  
  def setVariant(old: ItemVariant, newAmount: Long): ItemVariant =
    old.withComponents(DataComponentPatch.builder().set(kind, newAmount).build())
  
  override def insert(maxAmount: Long, transaction: TransactionContext): Long =
    val curAmount = getAmount
    val mayInsert = math.min(maxAmount, getCapacity - curAmount)
    val newAmount = mayInsert + curAmount
    val newVariant = setVariant(context.getItemVariant, newAmount)
    context.exchange(newVariant, 1, transaction)
    
    mayInsert

  override def extract(maxAmount: Long, transaction: TransactionContext): Long =
    val curAmount = getAmount
    val extracted = math.min(maxAmount, curAmount)
    val newAmount = curAmount - extracted
    val newVariant = setVariant(context.getItemVariant, newAmount)
    context.exchange(newVariant, 1, transaction)
    
    extracted
  
