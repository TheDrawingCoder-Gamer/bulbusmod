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
import net.minecraft.world.level.storage.ValueOutput

class StasisAccessorBlockEntity(pos: BlockPos, state: BlockState) extends StasisStorageBlockEntity(1, BulbusBlockEntities.stasisAccessor, pos, state), ClientSyncingBlockEntity:
  override val containerView: Container = new ContainerForStasisStorage

  override val containerStorage: ContainerStorage = ContainerStorage.of(containerView, null)

  def getStoredItem: ItemStack =
    items.get(0)

  override protected def logger: Logger = StasisAccessorBlockEntity.logger

  override protected def saveClientTag(output: ValueOutput): Unit =
    ContainerHelper.saveAllItems(output, this.items)

object StasisAccessorBlockEntity:
  val logger: Logger = LoggerFactory.getLogger(classOf[StasisAccessorBlockEntity])