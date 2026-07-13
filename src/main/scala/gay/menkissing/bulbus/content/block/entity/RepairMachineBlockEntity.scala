package gay.menkissing.bulbus.content.block.entity

import gay.menkissing.bulbus.api.XPStorage
import gay.menkissing.bulbus.registries.{BulbusBlockEntities, BulbusItems}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.{
  Transaction,
  TransactionContext
}
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.{ValueInput, ValueOutput}

import java.util
import scala.util.Using
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup
import net.minecraft.world.level.block.Block
import com.mojang.serialization.MapCodec
import net.minecraft.world.Containers
import net.minecraft.world.Clearable
import net.minecraft.core.particles.SimpleParticleType
import gay.menkissing.bulbus.registries.BulbusParticles
import net.minecraft.core.particles.ParticleTypes
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RepairMachineBlockEntity
  (pos: BlockPos, state: BlockState)
    extends BlockEntity(BulbusBlockEntities.repairMachine, pos, state), Clearable, ClientSyncingBlockEntity:
  private var heldGemIntl: ItemStack = ItemStack.EMPTY
  private var primaryItemIntl: ItemStack = ItemStack.EMPTY
  // only used in rendering, so not persistent
  var active: Boolean = false

  def heldGem: ItemStack = heldGemIntl
  def heldGem_=(value: ItemStack): Unit =
    heldGemIntl = value
    setChanged()
  
  def primaryItem: ItemStack = primaryItemIntl
  def primaryItem_=(value: ItemStack): Unit =
    primaryItemIntl = value
    setChanged()

  override def preRemoveSideEffects(pos: BlockPos, state: BlockState): Unit =
    if this.level != null then
      val pos = getBlockPos().getCenter()
      if !heldGem.isEmpty then
        Containers.dropItemStack(this.level, pos.x, pos.y, pos.z, heldGem)
      if !primaryItem.isEmpty then
        Containers.dropItemStack(this.level, pos.x, pos.y, pos.z, primaryItem)
    clearContent()

  override def clearContent(): Unit =
    this.heldGem = ItemStack.EMPTY
    this.primaryItem = ItemStack.EMPTY



  object HeldGemSlot extends SingleSlotStorage[ItemVariant]:
    override def extract
      (
        resource: ItemVariant,
        maxAmount: Long,
        transaction: TransactionContext
      ): Long =
      StoragePreconditions.notBlank(resource)
      if maxAmount > 1 then
        throw new IllegalStateException("Knowledge Gems only stack up to 1")
      if resource.matches(heldGem) then
        heldGem = ItemStack.EMPTY
        1
      else 0

    override def insert
      (
        resource: ItemVariant,
        maxAmount: Long,
        transaction: TransactionContext
      ): Long =
      StoragePreconditions.notBlank(resource)
      if maxAmount > 1 then
        throw new IllegalStateException("Knowledge Gems only stack up to 1")
      if heldGem.isEmpty && resource.is(BulbusItems.knowledgeStorage) then
        heldGem = resource.toStack(1)
        1
      else 0

    override def getAmount: Long = if heldGem.isEmpty then 0 else 1

    override def getCapacity: Long = 1

    override def getResource: ItemVariant = ItemVariant.of(heldGem)

    override def isResourceBlank: Boolean = heldGem.isEmpty

  object GemContext extends ContainerItemContext:
    override def getMainSlot: SingleSlotStorage[ItemVariant] = HeldGemSlot

    override def getAdditionalSlots: util.List[SingleSlotStorage[ItemVariant]] =
      util.List.of()

    override def insertOverflow
      (
        itemVariant: ItemVariant,
        maxAmount: Long,
        transactionContext: TransactionContext
      ): Long = 0L
    

  override def loadAdditional(input: ValueInput): Unit =
    super.loadAdditional(input)
    heldGem = input.read("held_gem", ItemStack.CODEC).orElse(ItemStack.EMPTY)
    primaryItem =
      input.read("primary_item", ItemStack.CODEC).orElse(ItemStack.EMPTY)
    active = input.getBooleanOr("active", false)

  override def saveAdditional(output: ValueOutput): Unit =
    super.saveAdditional(output)
    if !heldGem.isEmpty then output.store("held_gem", ItemStack.CODEC, heldGem)
    if !primaryItem.isEmpty then
      output.store("primary_item", ItemStack.CODEC, primaryItem)

  override protected def logger: Logger = RepairMachineBlockEntity.logger

  override protected def saveClientTag(output: ValueOutput): Unit =
    if !heldGem.isEmpty then output.store("held_gem", ItemStack.CODEC, heldGem)
    if !primaryItem.isEmpty then
      output.store("primary_item", ItemStack.CODEC, primaryItem)
    output.putBoolean("active", active)

object RepairMachineBlockEntity:
  val logger: Logger = LoggerFactory.getLogger(classOf[RepairMachineBlockEntity])

  val durabilityPerTick: Int = 6
  // ???
  // slightly better than mending (1.5x!)
  val durabilityPerXp: Int = 3

  object ClientTicker extends BlockEntityTicker[RepairMachineBlockEntity]:
    override def tick
      (
        level: Level,
        pos: BlockPos,
        state: BlockState,
        entity: RepairMachineBlockEntity
      ): Unit =
      // ok this part IS from enchanter
      if entity.active then
        val random = level.getRandom()
        if random.nextFloat() < 0.05f then
          val worldPos = entity.getBlockPos().getCenter()
          val randomX = -0.3f + random.nextFloat() * 0.6f
          val randomZ = -0.3f + random.nextFloat() * 0.6f
          val randomY = random.nextFloat() * 0.4f
          level.addParticle(
            BulbusParticles.repairParticle,
            worldPos.x + randomX,
            worldPos.y + 0.5 + randomY,
            worldPos.z + randomZ,
            0d,
            -0.1d,
            0d
          )

  object ServerTicker extends BlockEntityTicker[RepairMachineBlockEntity]:
    override def tick
      (
        level: Level,
        pos: BlockPos,
        state: BlockState,
        entity: RepairMachineBlockEntity
      ): Unit =
      val startedActive = entity.active
      entity.active = false
      if entity.heldGem.is(BulbusItems.knowledgeStorage) &&
        !entity.primaryItem.isEmpty &&
        entity.primaryItem.has(DataComponents.DAMAGE)
      then
        val remainingDamage =
          entity.primaryItem.get(DataComponents.DAMAGE).toInt
        if remainingDamage > 0 then
          val xpStorage =
            XPStorage.ITEM.find(entity.heldGem, entity.GemContext).nn
          val maxRepairable = math.min(durabilityPerTick, remainingDamage)
          val xpRepairAmount = xpStorage.getAmount * durabilityPerXp
          val realRepairable = math.min(xpRepairAmount, maxRepairable).toInt
          if realRepairable > 0 then
            val cost = Mth.positiveCeilDiv(realRepairable, durabilityPerXp)
            Using.resource(Transaction.openOuter()): transaction =>
              val actuallyDrained = xpStorage.extract(cost, transaction).toInt
              entity.active = true
              entity.primaryItem
                .set(DataComponents.DAMAGE, remainingDamage - realRepairable)
              transaction.commit()
      if startedActive != entity.active then
        entity.setChanged()
      