package gay.menkissing.bulbus.content.block.entity

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import gay.menkissing.bulbus.persistent.TuningChannel
import gay.menkissing.bulbus.components.StorageItemContents
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import gay.menkissing.bulbus.persistent.TunableStorageData
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import gay.menkissing.bulbus.registries.BulbusBlockEntities
import gay.menkissing.bulbus.content.block.entity.TunableTankBlockEntity.ImmutableContainer
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.storage.ValueInput

class TunableTankBlockEntity
  (pos: BlockPos, state: BlockState)
    extends BlockEntity(BulbusBlockEntities.tunableTank, pos, state),
      TunableBlockEntity:

  override protected def logger: Logger = TunableTankBlockEntity.logger

  override protected def clearChannelCache(): Unit =
    currentTank = None
    cachedContainer = None

  override protected def loadChannelData(tunable: TunableStorageData): Unit =
    currentTank = Some(tunable.getOrCreateTank(channel))

  var currentTank: Option[StorageItemContents.Builder[FluidVariant]] = None
  private var cachedContainer: Option[SingleSlotStorage[FluidVariant]] = None

  var clientContents: StorageItemContents[FluidVariant] = StorageItemContents.Fluid.DEFAULT

  def getContainer: SingleSlotStorage[FluidVariant] =
    cachedContainer match
      case Some(it) => it
      case None     =>
        if level.isClientSide then
          cachedContainer = Some(ImmutableContainer(clientContents))
          cachedContainer.get
        else
          if currentTank.isEmpty then
            currentTank = getTuner.map(_.getOrCreateTank(channel))
          val instance = ContainerInstance(currentTank.get)
          cachedContainer = Some(instance)
          instance

  override protected def loadAdditional(input: ValueInput): Unit =
    super.loadAdditional(input)
    if level != null && level.isClientSide then
      clientContents = input.read("tank", StorageItemContents.Fluid.CODEC).orElse(StorageItemContents.Fluid.DEFAULT)

  override protected def saveClientTag(output: ValueOutput): Unit =
    super.saveClientTag(output)
    currentTank.foreach: it =>
      output.store("tank", StorageItemContents.Fluid.CODEC, it.result)

  class ContainerInstance
    (val tank: StorageItemContents.Builder[FluidVariant])
      extends SnapshotParticipant[ResourceAmount[FluidVariant]],
        SingleSlotStorage[FluidVariant]:

    override protected def readSnapshot
      (snapshot: ResourceAmount[FluidVariant]): Unit =
      tank.amount = snapshot.amount()
      tank.template = snapshot.resource()

    override protected def createSnapshot(): ResourceAmount[FluidVariant] =
      ResourceAmount(tank.template, tank.amount)

    override def insert
      (
        resource: FluidVariant,
        maxAmount: Long,
        transaction: TransactionContext
      ): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)
      updateSnapshots(transaction)
      tank.insert(resource, maxAmount)

    override def getAmount(): Long = tank.amount

    override def isResourceBlank(): Boolean = tank.template.isBlank

    override def extract
      (
        resource: FluidVariant,
        maxAmount: Long,
        transaction: TransactionContext
      ): Long =
      StoragePreconditions.notBlankNotNegative(resource, maxAmount)
      updateSnapshots(transaction)
      tank.extract(resource, maxAmount)

    override def getResource(): FluidVariant = tank.template

    override def getCapacity(): Long = tank.max

    override protected def onFinalCommit(): Unit =
      TunableTankBlockEntity.this.setChanged()

object TunableTankBlockEntity:
  val logger: Logger = LoggerFactory.getLogger(classOf[TunableTankBlockEntity])

  class ImmutableContainer(val contents: StorageItemContents[FluidVariant]) extends SingleSlotStorage[FluidVariant]:

    override def insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long = 0

    override def isResourceBlank(): Boolean =  contents.isEmpty

    override def extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long = 0

    override def getResource(): FluidVariant = contents.variant

    override def supportsExtraction(): Boolean = false
    override def supportsInsertion(): Boolean = false

    override def getAmount(): Long = contents.amount
    override def getCapacity(): Long = TunableStorageData.Tank.capacity

