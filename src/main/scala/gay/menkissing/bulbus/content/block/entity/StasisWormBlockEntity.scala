package gay.menkissing.bulbus.content.block.entity

import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusTranslationKeys}
import gay.menkissing.bulbus.screen.StasisStorageMenu
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidStorage, FluidVariant}
import net.fabricmc.fabric.api.transfer.v1.item.{ItemStorage, ItemVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, Storage, StoragePreconditions, StorageView, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.base.{CombinedSlottedStorage, SingleSlotStorage}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

import java.util as ju
import scala.jdk.CollectionConverters.*

class StasisWormBlockEntity(pos: BlockPos, state: BlockState)
  extends ContainerStasisStorageBlockEntity(9, BulbusBlockEntities.stasisWorm, pos, state):
  override def defaultName: Component = Component.translatable(BulbusTranslationKeys.container.worm)

  // prevent looping sadness
  var isLocked: Boolean = false

  val ourItemStorage = CombinedSlottedStorage(itemStorages)
  val ourFluidStorage = CombinedSlottedStorage(fluidStorages)

  override val itemStorage: SlottedStorage[ItemVariant] = StasisWormBlockEntity.WormItemStorages(this)
  override val fluidStorage: SlottedStorage[FluidVariant] = StasisWormBlockEntity.WormFluidStorages(this)

  def withLock[T](block: => T): T =
    isLocked = true
    val res = block
    isLocked = false
    res

  def getFacing: Direction =
    getBlockState.getValue(BlockStateProperties.FACING)

  def getNextPos: BlockPos =
    val facing = getFacing
    getBlockPos.relative(facing)

  def isLooping: Boolean =
    val nextPos = getNextPos
    val entity = getLevel.getBlockEntity(nextPos, BulbusBlockEntities.stasisWorm)
    entity.map:
      _.isLocked
    .orElse(false)

  override def createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
    StasisStorageMenu.server(containerId, inventory, containerView)

object StasisWormBlockEntity:
  trait StorageMixin[T <: TransferVariant[?]] extends SlottedStorage[T]:
    val parent: StasisWormBlockEntity
    def storages: SlottedStorage[T]

    def lookup: BlockApiLookup[Storage[T], Direction | Null]

    def getNext: Option[SlottedStorage[T]] =
      if parent.isLooping then
        None
      else
        val nextPos = parent.getNextPos
        Option(lookup.find(parent.getLevel, nextPos, parent.getFacing.getOpposite)).flatMap:
          case slotted: SlottedStorage[T] => Some(slotted)
          case _ => None

    def withNext[R](f: SlottedStorage[T] => R): Option[R] =
      parent.withLock(getNext.map(f))

    override def getSlotCount: Int =
      storages.getSlotCount + withNext(_.getSlotCount).getOrElse(0)

    override def getSlot(slot: Int): SingleSlotStorage[T] =
      if slot < parent.capacity then
        storages.getSlot(slot)
      else
        withNext(_.getSlot(slot - parent.capacity)).getOrElse:
          throw new IndexOutOfBoundsException("Slot " + slot + " is out of bounds for this worm")

    override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      val amount = storages.extract(resource, maxAmount, transaction)
      if amount < maxAmount then
        amount + withNext(_.extract(resource, maxAmount - amount, transaction)).getOrElse(0L)
      else
        amount

    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      val amount = storages.insert(resource, maxAmount, transaction)
      if amount < maxAmount then
        amount + withNext(_.insert(resource, maxAmount - amount, transaction)).getOrElse(0L)
      else
        amount

    override def iterator(): ju.Iterator[StorageView[T]] =
      storages.iterator()
              .asScala
              .concat(
                withNext(_.iterator().asScala).getOrElse(Iterator.empty)
              ).asJava

  final class WormItemStorages(override val parent: StasisWormBlockEntity) extends StorageMixin[ItemVariant]:
    override def storages: SlottedStorage[ItemVariant] = parent.ourItemStorage

    override def lookup: BlockApiLookup[Storage[ItemVariant], Direction | Null] =
      // widen incorrect null detection
      ItemStorage.SIDED.asInstanceOf

  final class WormFluidStorages(override val parent: StasisWormBlockEntity) extends StorageMixin[FluidVariant]:
    override def storages: SlottedStorage[FluidVariant] = parent.ourFluidStorage

    override def lookup: BlockApiLookup[Storage[FluidVariant], Direction | Null] =
      FluidStorage.SIDED.asInstanceOf