package gay.menkissing.bulbus.content.block.entity.stasis_storage

import com.mojang.serialization.{Codec, Lifecycle}
import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.api.SingleTypeStorage
import gay.menkissing.bulbus.content.block.entity.{StasisStorageBlockEntity, StasisWormBlockEntity}
import gay.menkissing.bulbus.infra.lookup.{SingleTypeStorageLike, StasisStorage}
import gay.menkissing.bulbus.infra.lookup.base.{BulbusCombinedEnergyStorage, CombinedSingleTypeStorage, EmptySingleTypeStorage}
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidStorage, FluidVariant}
import net.fabricmc.fabric.api.transfer.v1.item.{ItemStorage, ItemVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.base.{CombinedSlottedStorage, SingleSlotStorage}
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, Storage, StoragePreconditions, StorageView, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.transaction.{Transaction, TransactionContext}
import net.minecraft.core.{Direction, Holder, MappedRegistry, NonNullList, Registry}
import net.minecraft.resources.{Identifier, ResourceKey}
import net.minecraft.world.item.ItemStack
import team.reborn.energy.api.{EnergyStorage, EnergyStorageUtil}

import java.util
import scala.util.Using

/**
 * An item forwarder abstracts over a storage that can be extracted from items
 * and end up being exposed to the world from a stasis storage.
 * @tparam Item the storage of a single item slot
 * @tparam World The block view for the stasis storage block
 * @tparam GenericWorld The base type of ALL storages in the block lookup
 */
trait StasisStorageItemForwarder[Item, World, GenericWorld]:
  def blockLookup: BlockApiLookup[GenericWorld, Direction | Null]
  
  def accepts(stack: ItemStack, dummyCtx: ContainerItemContext): Boolean =
    tryLoadStorage(stack, dummyCtx).isDefined

  /**
   * Try loading this forwarder's storage type from the item.
   * @param stack an Item
   * @param ctx the container context, for use with any ItemApiLookup calls
   * @return Either Some storage type, or None
   */
  def tryLoadStorage(stack: ItemStack, ctx: ContainerItemContext): Option[Item]

  /**
   * @return A default storage implementation that will effectively be a no op when exposed
   */
  def empty: Item

  /**
   * @return Some storage that allows insertion but deletes all items, or None if this forwarder shouldn't support that
   */
  def voiding: Option[Item] = None

  /**
   * Create something that can end up being submitted to some lookup api.
   * NOTE: It's not submitted by itself - you'll need to submit it yourself.
   * @param slots A list of the slots storages. When passed, it will all be empty.
   * @return
   */
  def createExposed(slots: NonNullList[Item]): World

  /**
   * Make a storage that will forward to the next block.
   * See [[StasisWormBlockEntity.StorageHelper]] for a base trait to use.
   * @param input
   * @param parent
   * @return
   */
  def wrapWorm(input: World, parent: StasisWormBlockEntity): World
  
  def transferer: ForwardingTransferer[World, GenericWorld] = ForwardingTransferer.passive


object StasisStorageItemForwarder:
  val registryKey: ResourceKey[Registry[StasisStorageItemForwarder[?, ?, ?]]] = ResourceKey.createRegistryKey(BulbusMod.locate("stasis_storage_item_forwarder"))
  val registry: Registry[StasisStorageItemForwarder[?, ?, ?]] = new MappedRegistry[StasisStorageItemForwarder[?, ?, ?]](registryKey, Lifecycle.stable())

  def register[A, B, C](id: Identifier, thingie: StasisStorageItemForwarder[A, B, C]): thingie.type =
    Registry.register(registry, id, thingie)
    thingie

  trait SingleTypeWormWrapHelper[T](val input: T) extends StasisWormBlockEntity.StorageHelper[T]:
    this: T =>
    protected def instance: SingleTypeStorageLike[T]

    def insert(maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notNegative(maxAmount)

      val amount = instance.insert(input)(maxAmount, transaction)
      if amount < maxAmount then
        amount + withNext(it => instance.insert(it)(maxAmount - amount, transaction)).getOrElse(0L)
      else
        amount

    def extract(maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notNegative(maxAmount)

      val amount = instance.extract(input)(maxAmount, transaction)
      if amount < maxAmount then
        amount + withNext(it => instance.extract(it)(maxAmount - amount, transaction)).getOrElse(0L)
      else
        amount

    def getAmount: Long =
      instance.getAmount(input) + withNext(instance.getAmount).getOrElse(0L)

    def getCapacity: Long =
      instance.getCapacity(input) + withNext(instance.getCapacity).getOrElse(0L)

  trait SingleTypeLikeForwarder[T] extends StasisStorageItemForwarder[T, T, T]:
    protected def instance: SingleTypeStorageLike[T]
    
    def itemLookup: ItemApiLookup[T, ContainerItemContext]

    override def tryLoadStorage(stack: ItemStack, ctx: ContainerItemContext): Option[T] =
      Option(ctx.find(itemLookup))

  trait SingleTypeForwarder extends SingleTypeLikeForwarder[SingleTypeStorage]:
    override def instance: SingleTypeStorageLike[SingleTypeStorage] = SingleTypeStorageLike.forSingleTypeStorage

    override def empty: SingleTypeStorage = EmptySingleTypeStorage

    override def createExposed(slots: NonNullList[SingleTypeStorage]): SingleTypeStorage =
      CombinedSingleTypeStorage(slots)

    override def wrapWorm(input: SingleTypeStorage, parentIn: StasisWormBlockEntity): SingleTypeStorage =
      new SingleTypeStorage with StasisWormBlockEntity.StorageHelper[SingleTypeStorage] with SingleTypeWormWrapHelper[SingleTypeStorage](input):
        override def instance: SingleTypeStorageLike[SingleTypeStorage] = SingleTypeStorageLike.forSingleTypeStorage

        override val parent: StasisWormBlockEntity = parentIn
        override def lookup: BlockApiLookup[SingleTypeStorage, Direction | Null] = blockLookup

  object EnergyForwarder extends SingleTypeLikeForwarder[EnergyStorage]:
    override def blockLookup: BlockApiLookup[EnergyStorage, Direction | Null] = EnergyStorage.SIDED.asInstanceOf

    override def itemLookup: ItemApiLookup[EnergyStorage, ContainerItemContext] = EnergyStorage.ITEM
    
    override def instance: SingleTypeStorageLike[EnergyStorage] = SingleTypeStorageLike.forEnergyStorage

    override def empty: EnergyStorage = EnergyStorage.EMPTY

    override def createExposed(slots: NonNullList[EnergyStorage]): EnergyStorage =
      BulbusCombinedEnergyStorage(slots)
    
    override def wrapWorm(input: EnergyStorage, parentIn: StasisWormBlockEntity): EnergyStorage =
      new EnergyStorage with StasisWormBlockEntity.StorageHelper[EnergyStorage] with SingleTypeWormWrapHelper[EnergyStorage](input):
        override def instance: SingleTypeStorageLike[EnergyStorage] = SingleTypeStorageLike.forEnergyStorage
        
        override val parent: StasisWormBlockEntity = parentIn

        override def lookup: BlockApiLookup[EnergyStorage, Direction | Null] = blockLookup

    // tech reborn energy is push based
    override val transferer: ForwardingTransferer[EnergyStorage, EnergyStorage] =
      (self, that) =>
        if self.supportsExtraction() && that.supportsInsertion() then
          Using.resource(Transaction.openOuter()): trans =>
            EnergyStorageUtil.move(self, that, Long.MaxValue, trans)
            trans.commit()
        

  trait StasisStorageForwarderMixin[I <: TransferVariant[?]] extends TaggedStasisStorageItemForwarder[StasisStorage[I], SlottedStorage[I], Storage[I], I]:
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

  object ItemForwarder extends StasisStorageItemForwarder.StasisStorageForwarderMixin[ItemVariant]:
    override def blockLookup: BlockApiLookup[Storage[ItemVariant], Direction | Null] = ItemStorage.SIDED.asInstanceOf

    override def itemLookup: ItemApiLookup[StasisStorage[ItemVariant], ContainerItemContext] = StasisStorage.item

    override def blank: ItemVariant = ItemVariant.blank()

    override def dataCodec: Codec[ItemVariant] = ItemVariant.CODEC

  object FluidForwarder extends StasisStorageItemForwarder.StasisStorageForwarderMixin[FluidVariant]:
    override def blockLookup: BlockApiLookup[Storage[FluidVariant], Direction | Null] = FluidStorage.SIDED.asInstanceOf

    override def itemLookup: ItemApiLookup[StasisStorage[FluidVariant], ContainerItemContext] = StasisStorage.fluid

    override def blank: FluidVariant = FluidVariant.blank()

    override def dataCodec: Codec[FluidVariant] = FluidVariant.CODEC

  val forItem: StasisStorageItemForwarder[StasisStorage[ItemVariant], SlottedStorage[ItemVariant], Storage[ItemVariant]] = register(BulbusMod.locate("item"), ItemForwarder)
  val forFluid: StasisStorageItemForwarder[StasisStorage[FluidVariant], SlottedStorage[FluidVariant], Storage[FluidVariant]] = register(BulbusMod.locate("fluid"), FluidForwarder)
  val forEnergy: StasisStorageItemForwarder[EnergyStorage, EnergyStorage, EnergyStorage] = register(BulbusMod.locate("energy"), EnergyForwarder)

  def init(): Unit = ()