package gay.menkissing.bulbus.content.block.entity

import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.content.item.{StasisBottleItem, StasisTubeItem}
import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusDataComponentTypes, BulbusItems, BulbusTags, BulbusTranslationKeys}
import gay.menkissing.bulbus.screen.{CommonStasisStorageMenu, StasisStorageMenu}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.{SlottedStorage, StoragePreconditions, TransferVariant}
import net.fabricmc.fabric.api.transfer.v1.storage.base.{CombinedSlottedStorage, SingleSlotStorage, SingleVariantItemStorage, SingleVariantStorage}
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.core.component.{DataComponentPatch, DataComponentType}
import net.minecraft.core.{BlockPos, HolderLookup, NonNullList}
import net.minecraft.network.chat.Component
import net.minecraft.sounds.{SoundEvent, SoundEvents, SoundSource}
import net.minecraft.world.entity.{ContainerUser, LivingEntity}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.{Container, ContainerHelper, Containers, MenuProvider}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType, ContainerOpenersCounter}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.storage.{ValueInput, ValueOutput}
import net.minecraft.world.phys.Vec3

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

abstract class StasisStorageBlockEntity(val capacity: Int, baseEntity: BlockEntityType[? <: StasisStorageBlockEntity], pos: BlockPos, state: BlockState)
  extends BlockEntity(baseEntity, pos, state):

  protected val items: NonNullList[ItemStack] = NonNullList.withSize(capacity, ItemStack.EMPTY)
  protected var lastInteractedSlot: Int = -1

  protected val itemStorages: NonNullList[StasisStorageBlockEntity.HasFilterable[ItemVariant]] = NonNullList.withSize(capacity, StasisStorageBlockEntity.EmptyItemSlot)
  protected val fluidStorages: NonNullList[StasisStorageBlockEntity.HasFilterable[FluidVariant]] = NonNullList.withSize(capacity, StasisStorageBlockEntity.EmptyFluidSlot)

  val itemStorage: CombinedSlottedStorage[ItemVariant, StasisStorageBlockEntity.HasFilterable[ItemVariant]] =
    CombinedSlottedStorage(itemStorages)

  val fluidStorage: CombinedSlottedStorage[FluidVariant, StasisStorageBlockEntity.HasFilterable[FluidVariant]] =
    CombinedSlottedStorage(fluidStorages)

  override def preRemoveSideEffects(pos: BlockPos, state: BlockState): Unit =
    if !state.is(getBlockState.getBlock) then
      StasisStorageBlockEntity.dropContents(getLevel, pos, this)

  def clearContent(): Unit =
    (0 until capacity).foreach(StorageManager.removeSlot)
    items.clear()

  def isEmpty: Boolean =
    items.stream().allMatch(_.isEmpty)

  def removeItem(slot: Int, amount: Int): ItemStack =
    val stack = this.items.get(slot)
    val newStack = stack.copyWithCount(stack.getCount - amount)
    this.items.set(slot, newStack)
    updateSlot(slot, getLevel.registryAccess())

    stack

  def setItem(slot: Int, stack: ItemStack): Unit =
    if StasisStorageBlockEntity.StorageTests.isAccepted(stack) then
      this.items.set(slot, stack)
      this.updateSlot(slot, getLevel.registryAccess())
    else if stack.isEmpty then
      this.removeItem(slot, 1)

  protected def updateSlot(slot: Int, registries: HolderLookup.Provider): Unit =
    val stack = this.items.get(slot)
    if StasisStorageBlockEntity.StorageTests.isTube(stack) then
      StorageManager.loadTubeSlot(slot, registries)
    else if StasisStorageBlockEntity.StorageTests.isBottle(stack) then
      StorageManager.loadBottleSlot(slot, registries)
    else if StasisStorageBlockEntity.StorageTests.isBattery(stack) then
      StorageManager.removeSlot(slot)
    else if StasisStorageBlockEntity.StorageTests.isGarbage(stack) then
      StorageManager.setVoidingSlot(slot)
    else
      StorageManager.removeSlot(slot)

  override protected def loadAdditional(input: ValueInput): Unit =
    super.loadAdditional(input)
    ContainerHelper.loadAllItems(input, items)
    (0 until capacity).foreach(it => updateSlot(it, input.lookup()))
    loadItemFilters(input)
    loadFluidFilters(input)

  override protected def saveAdditional(output: ValueOutput): Unit =
    super.saveAdditional(output)
    ContainerHelper.saveAllItems(output, items)
    saveItemFilters(output)
    saveFluidFilters(output)

  def loadItemFilters(input: ValueInput): Unit =
    input.childrenList(StasisStorageBlockEntity.tagItemFilters).ifPresent: list =>
      list.forEach: tag =>
        val j = tag.getByteOr("Slot", -1)
        if j >= 0 && j < capacity then
          tag.read("variant", ItemVariant.CODEC).ifPresent(itemStorages.get(j).setFilter)

  def loadFluidFilters(input: ValueInput): Unit =
    input.childrenListOrEmpty(StasisStorageBlockEntity.tagFluidFilters).forEach: tag =>
      val j = tag.getByteOr("Slot", -1)
      if j >= 0 && j < capacity then
        tag.read("variant", FluidVariant.CODEC).ifPresent(fluidStorages.get(j).setFilter)

  def saveItemFilters(output: ValueOutput): Unit =
    val list = output.childrenList(StasisStorageBlockEntity.tagItemFilters)
    (0 until capacity).foreach: i =>
      val filter = itemStorages.get(i).getFilter
      if !filter.isBlank then
        val tag = list.addChild()
        tag.putByte("Slot", i.toByte)
        tag.store("variant", ItemVariant.CODEC, filter)

  def saveFluidFilters(output: ValueOutput): Unit =
    val list = output.childrenList(StasisStorageBlockEntity.tagFluidFilters)
    (0 until capacity).foreach: i =>
      val filter = fluidStorages.get(i).getFilter
      if !filter.isBlank then
        val tag = list.addChild()
        tag.putByte("Slot", i.toByte)
        tag.store("variant", FluidVariant.CODEC, filter)

  object StorageManager:
    object item:
      def removeSlot(slot: Int): Unit =
        itemStorages.set(slot, StasisStorageBlockEntity.EmptyItemSlot)

      def unsetSlot(slot: Int): Unit =
        itemStorages.get(slot).unset()

      def setSlotFilter(slot: Int, variant: ItemVariant): Unit =
        itemStorages.get(slot).setFilter(variant)

      def loadTubeSlot(slot: Int, registries: HolderLookup.Provider): Unit =
        val stack = items.get(slot)
        val contents = stack.getOrDefault(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS, StorageItemContents.Item.DEFAULT)
        val max = StasisTubeItem.maxFromWorld(stack, registries)
        val storage = new StasisStorageBlockEntity.TubeItemStorage(StasisStorageBlockEntity.this, max, stack)
        storage.setFilter(contents.variant)
        itemStorages.set(slot, storage)

      def setVoidingSlot(slot: Int): Unit =
        itemStorages.set(slot, StasisStorageBlockEntity.VoidingItemSlot)

    object fluid:
      def removeSlot(slot: Int): Unit =
        fluidStorages.set(slot, StasisStorageBlockEntity.EmptyFluidSlot)

      def unsetSlot(slot: Int): Unit =
        fluidStorages.get(slot).unset()

      def setSlotFilter(slot: Int, variant: FluidVariant): Unit =
        fluidStorages.get(slot).setFilter(variant)

      def loadBottleSlot(slot: Int, registries: HolderLookup.Provider): Unit =
        val stack = items.get(slot)
        val contents = stack
          .getOrDefault(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS, StorageItemContents.Fluid.DEFAULT)
        val max = StasisBottleItem.getMaxFromWorld(stack, registries)
        val storage = new StasisStorageBlockEntity.BottleFluidStorage(StasisStorageBlockEntity.this, max, stack)
        storage.setFilter(contents.variant)
        fluidStorages.set(slot, storage)

      def setVoidingSlot(slot: Int): Unit =
        fluidStorages.set(slot, StasisStorageBlockEntity.VoidingFluidSlot)

    def removeSlot(slot: Int): Unit =
      item.removeSlot(slot)
      fluid.removeSlot(slot)

    def loadTubeSlot(slot: Int, registries: HolderLookup.Provider): Unit =
      fluid.removeSlot(slot)
      item.loadTubeSlot(slot, registries)

    def loadBottleSlot(slot: Int, registries: HolderLookup.Provider): Unit =
      item.removeSlot(slot)
      fluid.loadBottleSlot(slot, registries)

    def setVoidingSlot(slot: Int): Unit =
      item.setVoidingSlot(slot)
      fluid.setVoidingSlot(slot)

abstract class PlainContainerStasisStorageBlockEntity(capacity: Int, baseEntity: BlockEntityType[? <: StasisStorageBlockEntity], pos: BlockPos, state: BlockState)
  extends StasisStorageBlockEntity(capacity, baseEntity, pos, state), MenuProvider, NameableBlockEntity:
  protected val openSound: SoundEvent = SoundEvents.BARREL_OPEN
  protected val closeSound: SoundEvent = SoundEvents.BARREL_CLOSE

  override def getDisplayName: Component = getName

  def getContainerSize: Int = capacity

  def defaultName: Component

  def playSound(blockState: BlockState, soundEvent: SoundEvent): Unit =
    val vec3i = Vec3(1, 1, 1)
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

  val containerView: Container = new ContainerForStasisStorage

  class ContainerForStasisStorage extends Container:
    def parent: PlainContainerStasisStorageBlockEntity = PlainContainerStasisStorageBlockEntity.this

    def getContainerSize: Int = capacity

    override def clearContent(): Unit =
      items.clear()
      (0 until capacity).foreach(StorageManager.removeSlot)

    override def getItem(slot: Int): ItemStack =
      items.get(slot)

    override def isEmpty: Boolean = parent.isEmpty

    override def removeItem(slot: Int, count: Int): ItemStack =
      parent.removeItem(slot, count)

    override def removeItemNoUpdate(slot: Int): ItemStack =
      parent.removeItem(slot, 1)

    override def setChanged(): Unit =
      parent.setChanged()

    override def setItem(slot: Int, itemStack: ItemStack): Unit =
      parent.setItem(slot, itemStack)

    override def stillValid(player: Player): Boolean = Container.stillValidBlockEntity(parent, player)

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
  val tagFluidFilters = "fluid_filters"
  val tagItemFilters = "item_filters"

  trait HasFilterable[T] extends SlottedStorage[T]:
    def setFilter(filter: T): Unit
    def getFilter: T
    def unset(): Unit

  trait HasItemFilterable extends HasFilterable[ItemVariant]

  trait HasFluidFilterable extends HasFilterable[FluidVariant]

  trait NonextractableSlot[T] extends SingleSlotStorage[T], HasFilterable[T]:
    protected def getBlank: T
    override def setFilter(filter: T): Unit = ()
    override def getFilter: T = getBlank
    override def unset(): Unit = ()
    override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long = 0
    override def isResourceBlank: Boolean = true
    override def getResource: T = getBlank
    override def getAmount: Long = 0
    override def getCapacity: Long = 0

  abstract class FilterableStorage[T <: TransferVariant[?]](val parent: StasisStorageBlockEntity, val capacity: Long, val stack: ItemStack) extends SnapshotParticipant[StorageItemContents[T]], SingleSlotStorage[T], HasFilterable[T]:
    var filter: T = getBlankResource

    protected def getBlankResource: T

    val contentsType: DataComponentType[StorageItemContents[T]]

    def defaultContents: StorageItemContents[T]

    override def setFilter(filter: T): Unit = this.filter = filter

    override def getFilter: T = this.filter

    override def unset(): Unit = this.filter = getBlankResource

    protected def contents: StorageItemContents[T] =
      stack.getOrDefault(contentsType, defaultContents)

    protected def applyContents(contents: StorageItemContents[T]): Unit =
      stack.set(contentsType, contents)

    override def getCapacity: Long = capacity

    override def getResource: T =
      contents.variant

    override def isResourceBlank: Boolean = getResource.isBlank

    override def getAmount: Long =
      contents.amount

    def validVariant(storedVariant: T, resource: T): Boolean =
      if !storedVariant.isBlank then
        filter = storedVariant

      filter.isBlank || filter == resource

    override def createSnapshot(): StorageItemContents[T] = contents

    override def readSnapshot(snapshot: StorageItemContents[T]): Unit =
      applyContents(snapshot)

    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      val contents = this.contents

      val builder = StorageItemContents.Builder(contents.variant, contents.amount, capacity, getBlankResource)

      if validVariant(builder.template, resource) then
        this.updateSnapshots(transaction)
        val inserted = builder.insert(resource, maxAmount)
        applyContents(builder.result)

        inserted
      else
        0L

    override def extract(resource: T, maxAmount: Long, transaction: TransactionContext): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)

      val contents = this.contents

      val builder = StorageItemContents.Builder(contents.variant, contents.amount, capacity, getBlankResource)

      if validVariant(builder.template, resource) then
        this.updateSnapshots(transaction)
        val extracted = builder.extract(resource, maxAmount)
        applyContents(builder.result)

        extracted
      else
        0L



  final class TubeItemStorage(parent: StasisStorageBlockEntity, capacity: Long, context: ItemStack)
    extends FilterableStorage[ItemVariant](parent, capacity, context):

    override val contentsType: DataComponentType[StorageItemContents[ItemVariant]] = BulbusDataComponentTypes.STASIS_TUBE_CONTENTS

    override def defaultContents: StorageItemContents[ItemVariant] = StorageItemContents.Item.DEFAULT

    override def getBlankResource: ItemVariant = ItemVariant.blank()

  final class BottleFluidStorage(parent: StasisStorageBlockEntity, capacity: Long, context: ItemStack)
    extends FilterableStorage[FluidVariant](parent, capacity, context):

    override val contentsType: DataComponentType[StorageItemContents[FluidVariant]] = BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS

    override def defaultContents: StorageItemContents[FluidVariant] = StorageItemContents.Fluid.DEFAULT

    override def getBlankResource: FluidVariant = FluidVariant.blank()

  trait EmptySlot[T] extends NonextractableSlot[T]:
    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long = 0


  trait VoidingSlot[T] extends NonextractableSlot[T]:
    override def insert(resource: T, maxAmount: Long, transaction: TransactionContext): Long = maxAmount

  object VoidingItemSlot extends VoidingSlot[ItemVariant], HasItemFilterable:
    override protected def getBlank: ItemVariant = ItemVariant.blank()

  object VoidingFluidSlot extends VoidingSlot[FluidVariant], HasFluidFilterable:
    override protected def getBlank: FluidVariant = FluidVariant.blank()


  object EmptyItemSlot extends EmptySlot[ItemVariant], HasItemFilterable:
    override protected def getBlank: ItemVariant = ItemVariant.blank()

  object EmptyFluidSlot extends EmptySlot[FluidVariant], HasFluidFilterable:
    override protected def getBlank: FluidVariant = FluidVariant.blank()

  object StorageTests:
    def isAccepted(stack: ItemStack): Boolean =
      isBattery(stack) || isTube(stack) || isBottle(stack) || isGarbage(stack)

    def isBattery(stack: ItemStack): Boolean =
      stack.is(BulbusItems.stasisBattery)

    def isTube(stack: ItemStack): Boolean =
      stack.is(BulbusItems.stasisTube)

    def isBottle(stack: ItemStack): Boolean =
      stack.is(BulbusItems.stasisBottle)

    def isGarbage(stack: ItemStack): Boolean =
      stack.is(BulbusTags.item.voidsInsertInShelf)

  def dropContents(level: Level, pos: BlockPos, entity: StasisStorageBlockEntity): Unit =
      Containers.dropContents(level, pos, entity.items)
      entity.items.clear()

  final class StasisShelfBlockEntity(pos: BlockPos, state: BlockState)
    extends ContainerStasisStorageBlockEntity(9, BulbusBlockEntities.stasisShelf, pos, state):
    override def defaultName: Component = Component.translatable(BulbusTranslationKeys.container.shelf)

    override def createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
      StasisStorageMenu.server(containerId, inventory, containerView)