package gay.menkissing.bulbus.content.block.entity

import com.mojang.serialization.Codec
import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.content.block.entity.stasis_storage.{ForwardingTransferer, StasisStorageItemForwarder, TaggedStasisStorageItemForwarder}
import gay.menkissing.bulbus.content.item.{StasisBottleItem, StasisTubeItem}
import gay.menkissing.bulbus.infra.lookup.{SingleTypeStorageLike, StasisStorage}
import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusBlocks, BulbusBuiltInRegistries, BulbusDataComponentTypes, BulbusItems, BulbusSounds, BulbusTags, BulbusTranslationKeys}
import gay.menkissing.bulbus.screen.{CommonStasisStorageMenu, StasisStorageMenu}
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidStorage, FluidVariant}
import net.fabricmc.fabric.api.transfer.v1.item.{ContainerStorage, ItemStorage, ItemVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, Storage, StoragePreconditions, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.base.{CombinedSlottedStorage, SingleSlotStorage, SingleVariantItemStorage, SingleVariantStorage}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.core.component.{DataComponentPatch, DataComponentType}
import net.minecraft.core.{BlockPos, Direction, Holder, HolderLookup, NonNullList}
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.{SoundEvent, SoundEvents, SoundSource}
import net.minecraft.world.entity.{ContainerUser, LivingEntity}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.{Clearable, Container, ContainerHelper, Containers, MenuProvider}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType, ContainerOpenersCounter, ListBackedContainer}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.storage.{ValueInput, ValueOutput}

import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import scala.collection.mutable

abstract class StasisStorageBlockEntity(val capacity: Int, baseEntity: BlockEntityType[? <: StasisStorageBlockEntity], pos: BlockPos, state: BlockState)
  extends BlockEntity(baseEntity, pos, state), Clearable:

  protected val items: NonNullList[ItemStack] = NonNullList.withSize(capacity, ItemStack.EMPTY)
  protected var lastInteractedSlot: Int = -1

  protected val forwardedStorages: Map[Identifier, NonNullList[?]] =
    BulbusBuiltInRegistries.itemForwarder.entrySet.stream().map: entry =>
      (entry.getKey.identifier(), NonNullList.withSize(capacity, entry.getValue.empty))
    .toScala(Map)

  protected val forwardedStorage: Map[Identifier, ?] =
    BulbusBuiltInRegistries.itemForwarder.entrySet.stream().map: entry =>
      (entry.getKey.identifier(), entry.getValue.createExposed(forwardedStorages(entry.getKey.identifier()).asInstanceOf))
    .toScala(Map)


  protected def getForwardedStorages[T](forwarder: StasisStorageItemForwarder[T, ?, ?]): NonNullList[T] =
    val id = BulbusBuiltInRegistries.itemForwarder.getKey(forwarder).nn
    forwardedStorages(id).asInstanceOf[NonNullList[T]]

  def getForwardedStorage[S](forwarder: StasisStorageItemForwarder[?, S, ?]): S =
    val id = BulbusBuiltInRegistries.itemForwarder.getKey(forwarder).nn
    forwardedStorage(id).asInstanceOf[S]


  override def preRemoveSideEffects(pos: BlockPos, state: BlockState): Unit =
    if level != null then
      StasisStorageBlockEntity.dropContents(getLevel, pos, this)

  // implementing clearable fixes some mods
  // on 1.21 i specifically implemented this to fix aeronautics, but any mod that
  // wants to remove this entity from the world without it dropping its items, it can use this
  def clearContent(): Unit =
    (0 until capacity).foreach(StorageManager.removeSlot)
    items.clear()

  def isEmpty: Boolean =
    items.stream().allMatch(_.isEmpty)

  // protected: do this via the containerView
  protected def removeItem(slot: Int, amount: Int): ItemStack =
    val stack = this.items.get(slot)
    val newStack = stack.copyWithCount(stack.getCount - amount)
    this.items.set(slot, newStack)
    updateSlot(slot)

    stack

  protected def setItem(slot: Int, stack: ItemStack): Unit =
    if StasisStorageBlockEntity.StorageTests.isAccepted(stack) then
      this.items.set(slot, stack)
      this.updateSlot(slot)
    else if stack.isEmpty then
      this.removeItem(slot, 1)

  protected def updateSlot(slot: Int): Unit =
    val stack = this.items.get(slot)
    if StasisStorageBlockEntity.StorageTests.isGarbage(stack) then
      StorageManager.setVoidingSlot(slot)
    else if stack.isEmpty then
      StorageManager.removeSlot(slot)
    else
      StorageManager.loadStorageSlot(slot)

  override protected def loadAdditional(input: ValueInput): Unit =
    super.loadAdditional(input)
    if level != null && level.isClientSide then
      this.items.clear()
      ContainerHelper.loadAllItems(input, items)
    else
      this.clearContent()
      ContainerHelper.loadAllItems(input, items)
      (0 until capacity).foreach(it => updateSlot(it))
      loadForwarderData(input)


  override protected def saveAdditional(output: ValueOutput): Unit =
    super.saveAdditional(output)
    ContainerHelper.saveAllItems(output, items)
    saveForwarderData(output)

  def loadForwarderData(input: ValueInput): Unit =
    input.child("fwd_data").ifPresent: it =>
      BulbusBuiltInRegistries.itemForwarder.forEach:
        case fwdr: TaggedStasisStorageItemForwarder[?, ?, ?, ?] =>
          val id = BulbusBuiltInRegistries.itemForwarder.getKey(fwdr).nn
          it.childrenListOrEmpty(id.toString).forEach: tag =>
            val j = tag.getByteOr("Slot", -1)
            if j >= 0 && j < capacity then
              tag.read("data", fwdr.dataCodec).ifPresent: data =>
                val storage = getForwardedStorages(fwdr).get(j)
                fwdr.loadData(storage, data)
        case _ => ()

  def saveForwarderData(output: ValueOutput): Unit =
    val tag = output.child("fwd_data")
    BulbusBuiltInRegistries.itemForwarder.forEach:
      case fwdr: TaggedStasisStorageItemForwarder[?, ?, ?, ?] =>
        val id = BulbusBuiltInRegistries.itemForwarder.getKey(fwdr).nn
        val subtag = tag.childrenList(id.toString)
        val storage = getForwardedStorages(fwdr)
        (0 until capacity).foreach: i =>
          fwdr.constructData(storage.get(i)).foreach: data =>
            val child = subtag.addChild()
            child.putByte("Slot", i.toByte)
            child.store("data", fwdr.dataCodec, data)

      case _ => ()


  val containerView: Container

  val containerStorage: ContainerStorage

  val fabricContainer: Container = new FabricContainerForStasisStorage
  
  val fabricContainerStorage: ContainerStorage = ContainerStorage.of(fabricContainer, null)
  

  // dont ask
  class FabricContainerForStasisStorage extends ListBackedContainer:
    override def getItems: NonNullList[ItemStack] = items

    override def setChanged(): Unit = StasisStorageBlockEntity.this.setChanged()

    override def stillValid(player: Player): Boolean = Container.stillValidBlockEntity(StasisStorageBlockEntity.this, player)

  class ContainerForStasisStorage extends ListBackedContainer:
    def parent: StasisStorageBlockEntity = StasisStorageBlockEntity.this

    override def getItems: NonNullList[ItemStack] = items

    override def clearContent(): Unit =
      super.clearContent()
      (0 until capacity).foreach(StorageManager.removeSlot)

    override def acceptsItemType(itemStack: ItemStack): Boolean =
      StasisStorageBlockEntity.StorageTests.isAccepted(itemStack)

    override def removeItem(slot: Int, count: Int): ItemStack =
      val result = super.removeItem(slot, count)
      updateSlot(slot)
      result

    override def setItemNoUpdate(slot: Int, itemStack: ItemStack): Unit =
      val oldItem = getItem(slot)
      super.setItemNoUpdate(slot, itemStack)
      // only update it if we actually changed please
      if oldItem.getItem != itemStack.getItem then
        updateSlot(slot)

    override def setChanged(): Unit =
      parent.setChanged()

    override def stillValid(player: Player): Boolean = Container.stillValidBlockEntity(parent, player)
  
  object StorageManager:
    def removeSlot(slot: Int): Unit =
      BulbusBuiltInRegistries.itemForwarder.forEach: fwdr =>
        val storages = getForwardedStorages(fwdr)
        storages.set(slot, fwdr.empty)
      
    def loadStorageSlot(slot: Int): Unit =
      val stack = items.get(slot)
      val ctx = ContainerItemContext.ofSingleSlot(fabricContainerStorage.getSlot(slot))
      removeSlot(slot)
      BulbusBuiltInRegistries.itemForwarder.forEach: fwdr =>
        val storages = getForwardedStorages(fwdr)
        val res = fwdr.tryLoadStorage(stack, ctx)
        res.foreach: it =>
          storages.set(slot, it)
          
          
      

    def setVoidingSlot(slot: Int): Unit =
      BulbusBuiltInRegistries.itemForwarder.forEach: fwdr =>
        val storages = getForwardedStorages(fwdr)
        fwdr.voiding match
          case Some(voiding) =>
            storages.set(slot, voiding)
          case _ =>
            storages.set(slot, fwdr.empty)

abstract class PlainContainerStasisStorageBlockEntity(capacity: Int, baseEntity: BlockEntityType[? <: StasisStorageBlockEntity], pos: BlockPos, state: BlockState)
  extends StasisStorageBlockEntity(capacity, baseEntity, pos, state), MenuProvider, NameableBlockEntity:
  protected val openSound: SoundEvent
  protected val closeSound: SoundEvent

  override def getDisplayName: Component = getName

  def getContainerSize: Int = capacity

  def defaultName: Component

  def playSound(blockState: BlockState, soundEvent: SoundEvent): Unit =
    val direction = blockState.getValueOrElse(BlockStateProperties.FACING, Direction.UP)
    val vec3i = direction.getUnitVec3
    val d = this.worldPosition.getX.toDouble + 0.5 + (vec3i.x / 2)
    val e = this.worldPosition.getY.toDouble + 0.5 + (vec3i.y / 2)
    val f = this.worldPosition.getZ.toDouble + 0.5 + (vec3i.z / 2)
    this.level.playSound(
      null,
      d, e, f,
      soundEvent,
      SoundSource.BLOCKS,
      0.5f,
      this.level.getRandom.nextFloat() * 0.1f + 0.9f
    )



abstract class ContainerStasisStorageBlockEntity(capacity: Int, baseEntity: BlockEntityType[? <: ContainerStasisStorageBlockEntity], pos: BlockPos, state: BlockState)
  extends PlainContainerStasisStorageBlockEntity(capacity, baseEntity, pos, state):

  def updateBlockState(state: BlockState, open: Boolean): Unit =
    this.level.setBlockAndUpdate(getBlockPos, state.setValue(BlockStateProperties.OPEN, open))

  def recheckOpen(): Unit =
    if !this.remove then
      openersCounter.recheckOpeners(this.getLevel, this.getBlockPos, this.getBlockState)

  private def incrementOpeners(user: ContainerUser): Unit =
    if !user.getLivingEntity.isSpectator then
      this.openersCounter.incrementOpeners(
        user.getLivingEntity,
        getLevel,
        getBlockPos,
        getBlockState,
        user.getContainerInteractionRange
      )

  private def decrementOpeners(user: ContainerUser): Unit =
    if !user.getLivingEntity.isSpectator then
      this.openersCounter.decrementOpeners(
        user.getLivingEntity,
        getLevel,
        getBlockPos,
        getBlockState,
      )

  override val containerView: Container = new OpenableContainerForStasisStorage

  class OpenableContainerForStasisStorage extends ContainerForStasisStorage:
    override def startOpen(containerUser: ContainerUser): Unit =
      if !remove then
        incrementOpeners(containerUser)

    override def stopOpen(containerUser: ContainerUser): Unit =
      if !remove then
        decrementOpeners(containerUser)

  private val openersCounter: ContainerOpenersCounter =
    new ContainerOpenersCounter:
      override def onOpen(level: Level, pos: BlockPos, blockState: BlockState): Unit =
        playSound(blockState, openSound)
        updateBlockState(blockState, true)

      override def onClose(level: Level, pos: BlockPos, blockState: BlockState): Unit =
        playSound(blockState, closeSound)
        updateBlockState(blockState, false)

      override def openerCountChanged(level: Level, pos: BlockPos, blockState: BlockState, previous: Int, current: Int): Unit = ()

      override def isOwnContainer(player: Player): Boolean =
        player.containerMenu match
          case menu: CommonStasisStorageMenu =>
            menu.container match
              case cbfs: ContainerForStasisStorage =>
                cbfs.parent == ContainerStasisStorageBlockEntity.this
              case _ => false
          case _ => false

object StasisStorageBlockEntity:
  
  object ServerTicker extends BlockEntityTicker[StasisStorageBlockEntity]:
    override def tick(level: Level, pos: BlockPos, state: BlockState, entity: StasisStorageBlockEntity): Unit =
      BulbusBuiltInRegistries.itemForwarder.forEach: forwarder =>
        // check to skip trivial case
        if forwarder.transferer ne ForwardingTransferer.passive then
          val self = entity.getForwardedStorage(forwarder)
          Direction.values().foreach: dir =>
            val newPos = pos.relative(dir)
            if !posIsTargetOfWorm(level, newPos) then
                val storage = forwarder.blockLookup.find(level, newPos, dir)
                if storage != null then
                  forwarder.transferer.transfer(self, storage)

  def posIsTargetOfWorm(level: Level, pos: BlockPos): Boolean =
    Direction.values().exists: dir =>
      val newPos = pos.relative(dir)
      val newState = level.getBlockState(pos)
      newState.is(BulbusBlocks.stasisWorm) && newState.getValue(BlockStateProperties.FACING).getOpposite == dir

  trait NonextractableSlot[T] extends SingleSlotStorage[T], StasisStorage[T]:
    protected def getBlank: T
    override def setFilter(filter: T): Unit = ()
    override def getFilter: T = getBlank
    override def unsetFilter(): Unit = ()
    override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long = 0
    override def isResourceBlank: Boolean = true
    override def getResource: T = getBlank
    override def getAmount: Long = 0
    override def getCapacity: Long = 0
  
  
  trait EmptySlot[T] extends NonextractableSlot[T]:
    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long = 0


  trait VoidingSlot[T] extends NonextractableSlot[T]:
    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long = maxAmount
  

  object StorageTests:
    def isAccepted(stack: ItemStack): Boolean =
      isGarbage(stack) || isStasisStorage(stack)

    def isStasisStorage(stack: ItemStack): Boolean =
      val ctx = ContainerItemContext.withConstant(stack)
      BulbusBuiltInRegistries.itemForwarder.stream().anyMatch(_.accepts(stack, ctx))

    def isGarbage(stack: ItemStack): Boolean =
      stack.is(BulbusTags.item.voidsInsertInShelf)

  def dropContents(level: Level, pos: BlockPos, entity: StasisStorageBlockEntity): Unit =
      Containers.dropContents(level, pos, entity.items)
      entity.items.clear()

  final class StasisShelfBlockEntity(pos: BlockPos, state: BlockState)
    extends ContainerStasisStorageBlockEntity(9, BulbusBlockEntities.stasisShelf, pos, state):
    override val containerStorage: ContainerStorage = ContainerStorage.of(containerView, null)

    override def defaultName: Component = Component.translatable(BulbusTranslationKeys.container.shelf)

    override def createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
      StasisStorageMenu.server(containerId, inventory, containerView)

    override protected val openSound: SoundEvent = BulbusSounds.stasisShelfOpen
    override protected val closeSound: SoundEvent = BulbusSounds.stasisShelfClose