package gay.menkissing.bulbus.registries

import com.mojang.serialization.Codec
import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.content.block.entity.StasisWormBlockEntity
import gay.menkissing.bulbus.content.block.entity.stasis_storage.{ForwardingTransferer, StasisStorageItemForwarder}
import gay.menkissing.bulbus.content.block.entity.stasis_storage.StasisStorageItemForwarder.{SingleTypeLikeForwarder, SingleTypeWormWrapHelper}
import gay.menkissing.bulbus.infra.lookup.base.BulbusCombinedEnergyStorage
import gay.menkissing.bulbus.infra.lookup.{SingleTypeStorageLike, StasisStorage}
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidStorage, FluidVariant}
import net.fabricmc.fabric.api.transfer.v1.item.{ItemStorage, ItemVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, Storage}
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.{Direction, NonNullList, Registry}
import net.minecraft.resources.Identifier
import team.reborn.energy.api.{EnergyStorage, EnergyStorageUtil}

import scala.util.Using

object BulbusItemForwarders:

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

  def register[A, B, C](id: Identifier, thingie: StasisStorageItemForwarder[A, B, C]): thingie.type =
    Registry.register(BulbusBuiltInRegistries.itemForwarder, id, thingie)
    thingie
  
  val forItem: StasisStorageItemForwarder[StasisStorage[ItemVariant], SlottedStorage[ItemVariant], Storage[ItemVariant]] = register(BulbusMod
    .locate("item"), ItemForwarder)
  val forFluid: StasisStorageItemForwarder[StasisStorage[FluidVariant], SlottedStorage[FluidVariant], Storage[FluidVariant]] = register(BulbusMod
    .locate("fluid"), FluidForwarder)
  val forEnergy: StasisStorageItemForwarder[EnergyStorage, EnergyStorage, EnergyStorage] = register(BulbusMod
    .locate("energy"), EnergyForwarder)
  
  def init(): Unit = ()
