package gay.menkissing.bulbus.content.block.entity

import net.minecraft.world.level.block.entity.BlockEntity
import gay.menkissing.bulbus.persistent.TuningChannel
import gay.menkissing.bulbus.persistent.TunableStorageData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.slf4j.Logger
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup.Provider
import scala.util.Using
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.storage.TagValueOutput

trait TunableBlockEntity extends ClientSyncingBlockEntity:
  protected var channelIntl: TuningChannel = TuningChannel.DEFAULT

  protected def clearChannelCache(): Unit
  protected def loadChannelData(tunable: TunableStorageData): Unit


  override def setChanged(): Unit =
    super.setChanged()
    getTuner.foreach(_.setDirty())

  def channel: TuningChannel = channelIntl
  def channel_=(value: TuningChannel): Unit =
    channelIntl = value
    clearChannelCache()
    getTuner.foreach(loadChannelData)

    setChanged()

  override protected def loadAdditional(input: ValueInput): Unit =
    super.loadAdditional(input)
    channel =
      input.read("channel", TuningChannel.CODEC).orElse(TuningChannel.DEFAULT)

  override protected def saveAdditional(output: ValueOutput): Unit =
    super.saveAdditional(output)
    output.store("channel", TuningChannel.CODEC, channel)

  override protected def saveClientTag(output: ValueOutput): Unit =
    output.store("channel", TuningChannel.CODEC, channel)
  

  def getTuner: Option[TunableStorageData] =
    getLevel match
      case null => None
      case serverLevel: ServerLevel =>
        val server = serverLevel.getServer()
        val tunable = TunableStorageData.get(server)
        Some(tunable)
      case _ => None
