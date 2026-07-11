package gay.menkissing.bulbus.content.block.entity

import gay.menkissing.bulbus.registries.BulbusBlockEntities
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage
import net.minecraft.core.{BlockPos, HolderLookup}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.{ClientGamePacketListener, ClientboundBlockEntityDataPacket}
import net.minecraft.util.ProblemReporter
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.TagValueOutput
import net.minecraft.world.{Container, ContainerHelper}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Using

class StasisAccessorBlockEntity(pos: BlockPos, state: BlockState) extends StasisStorageBlockEntity(1, BulbusBlockEntities.stasisAccessor, pos, state):
  override val containerView: Container = new ContainerForStasisStorage

  override val containerStorage: ContainerStorage = ContainerStorage.of(containerView, null)

  def getStoredItem: ItemStack =
    items.get(0)

  override def setChanged(): Unit =
    super.setChanged()
    if this.level != null && !this.level.isClientSide then
      this.level.sendBlockUpdated(this.getBlockPos, this.getBlockState, this.getBlockState, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS)

  override def getUpdatePacket: Packet[ClientGamePacketListener] =
    ClientboundBlockEntityDataPacket.create(this)

  override def getUpdateTag(registries: HolderLookup.Provider): CompoundTag =
    Using.resource(ProblemReporter.ScopedCollector(this.problemPath(), StasisAccessorBlockEntity.Logger)): reporter =>
      val output = TagValueOutput.createWithContext(reporter, registries)
      ContainerHelper.saveAllItems(output, this.items)
      output.buildResult()
object StasisAccessorBlockEntity:
  val Logger: Logger = LoggerFactory.getLogger(classOf[StasisAccessorBlockEntity])