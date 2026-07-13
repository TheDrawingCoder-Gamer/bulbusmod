package gay.menkissing.bulbus.content.block.entity

import org.slf4j.Logger
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.storage.TagValueOutput
import scala.util.Using
import net.minecraft.world.level.block.Block

trait ClientSyncingBlockEntity extends BlockEntity:
  protected def logger: Logger

  protected def saveClientTag(output: ValueOutput): Unit

  override def setChanged(): Unit =
    super.setChanged()
    if getLevel != null && !getLevel.isClientSide then
      getLevel.sendBlockUpdated(getBlockPos, getBlockState, getBlockState, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS)

  override def getUpdatePacket(): Packet[ClientGamePacketListener] | Null =
    ClientboundBlockEntityDataPacket.create(this)
  
  override def getUpdateTag(registries: Provider): CompoundTag =
    Using.resource(ProblemReporter.ScopedCollector(this.problemPath(), logger)): reporter =>
      val output = TagValueOutput.createWithContext(reporter, registries)
      saveClientTag(output)
      output.buildResult()

