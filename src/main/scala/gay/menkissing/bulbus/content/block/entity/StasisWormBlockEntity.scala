package gay.menkissing.bulbus.content.block.entity

import gay.menkissing.bulbus.content.block.entity.stasis_storage.StasisStorageItemForwarder
import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusBuiltInRegistries, BulbusSounds, BulbusTranslationKeys}
import gay.menkissing.bulbus.screen.StasisStorageMenu
import gay.menkissing.bulbus.util.storage.StorageSlotinator
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidStorage, FluidVariant}
import net.fabricmc.fabric.api.transfer.v1.item.{ContainerStorage, ItemStorage, ItemVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, Storage, StoragePreconditions, StorageView, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.base.{CombinedSlottedStorage, SingleSlotStorage}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

import java.util as ju
import scala.jdk.CollectionConverters.*

class StasisWormBlockEntity(pos: BlockPos, state: BlockState)
  extends ContainerStasisStorageBlockEntity(9, BulbusBlockEntities.stasisWorm, pos, state):
  override def defaultName: Component = Component.translatable(BulbusTranslationKeys.container.worm)

  override protected val openSound: SoundEvent = BulbusSounds.stasisWormOpen
  override protected val closeSound: SoundEvent = BulbusSounds.stasisWormClose

  // prevent looping sadness
  var isLocked: Boolean = false

  val wormForwardedStorage: Map[Identifier, ?] =
    BulbusBuiltInRegistries.itemForwarder.entrySet().asScala.iterator.map: entry =>
      entry.getKey.identifier() -> entry.getValue.wrapWorm(forwardedStorage(entry.getKey.identifier()).asInstanceOf, this)
    .toMap

  override val containerStorage: ContainerStorage = ContainerStorage.of(containerView, null)
  
  override def getForwardedStorage[S](forwarder: StasisStorageItemForwarder[?, S, ?]): S =
    val id = BulbusBuiltInRegistries.itemForwarder.getKey(forwarder)
    wormForwardedStorage(id).asInstanceOf[S]

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
  trait StorageHelper[T]:
    protected val parent: StasisWormBlockEntity

    protected def lookup: BlockApiLookup[T, Direction | Null]

    protected def getNext: Option[T] =
      if parent.isLooping then
        None
      else
        val nextPos = parent.getNextPos
        Option(lookup.find(parent.getLevel, nextPos, parent.getFacing.getOpposite))

    protected def withNext[R](f: T => R): Option[R] =
      parent.withLock(getNext.map(f))

  trait StorageMixin[T <: TransferVariant[?]] extends SlottedStorage[T], StorageHelper[Storage[T]]:
    def storages: SlottedStorage[T]

    override def getNext: Option[SlottedStorage[T]] =
      if parent.isLooping then
        None
      else
        val nextPos = parent.getNextPos
        Option(lookup.find(parent.getLevel, nextPos, parent.getFacing.getOpposite)).map:
          case slotted: SlottedStorage[T] => slotted
          case evil => StorageSlotinator[T](evil)

    protected def withNextSlotted[R](f: SlottedStorage[T] => R): Option[R] =
      parent.withLock(getNext.map(f))

    override def getSlotCount: Int =
      storages.getSlotCount + withNextSlotted(_.getSlotCount).getOrElse(0)

    override def getSlot(slot: Int): SingleSlotStorage[T] =
      if slot < parent.capacity then
        storages.getSlot(slot)
      else
        withNextSlotted(_.getSlot(slot - parent.capacity)).getOrElse:
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
      // we MUST evaluate this now to prevent looping
      val nextIterator = withNext(_.iterator().asScala).getOrElse(Iterator.empty)
      storages.iterator()
              .asScala
              .concat(
                nextIterator
              ).asJava
