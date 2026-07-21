package gay.menkissing.bulbus.content.block.entity

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.storage.ValueInput
import gay.menkissing.bulbus.persistent.TuningChannel
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.core.NonNullList
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import gay.menkissing.bulbus.persistent.TunableStorageData
import net.minecraft.world.level.block.entity.ListBackedContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.ContainerHelper
import net.minecraft.world.Container
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup.Provider
import scala.util.Using
import net.minecraft.util.ProblemReporter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import net.minecraft.world.level.storage.TagValueOutput
import net.minecraft.world.level.block.Block
import net.minecraft.world.Containers
import net.minecraft.world.MenuProvider
import net.minecraft.network.chat.Component
import gay.menkissing.bulbus.registries.BulbusTranslationKeys
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu
import gay.menkissing.bulbus.registries.BulbusBlockEntities

class TunableChestBlockEntity
  (pos: BlockPos, state: BlockState)
    extends BlockEntity(BulbusBlockEntities.tunableChest, pos, state), TunableBlockEntity:
  var currentList: Option[NonNullList[ItemStack]] = None
  private var cachedContainer: Option[ContainerInstance] = None

  override protected def logger: Logger = TunableChestBlockEntity.logger

  override protected def clearChannelCache(): Unit =
    currentList = None
    cachedContainer = None
  
  override protected def loadChannelData(tunable: TunableStorageData): Unit =
    currentList = Some(tunable.getOrCreateSlots(channel))

  def getContainer: ContainerInstance | Null =
    cachedContainer match
      case Some(it) => it
      case None =>
        // for jade (sorry jade : /)
        if level.isClientSide then
          null
        else
          if currentList.isEmpty then
            currentList = getTuner.map(_.getOrCreateSlots(channel))
          val instance = new ContainerInstance(channel, currentList.get)
          cachedContainer = Some(instance)
          instance
          

  // We can't be clearable, so we can't directly implement container
  class ContainerInstance(openedWith: TuningChannel, override val getItems: NonNullList[ItemStack]) extends ListBackedContainer, MenuProvider:
    def getDisplayName: Component =
      Component.translatable(BulbusTranslationKeys.container.tunableChest)
    
    override def createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
      ChestMenu.threeRows(containerId, inventory, this)


    override def stillValid(player: Player): Boolean = 
      openedWith == channel && Container.stillValidBlockEntity(TunableChestBlockEntity.this, player)

    override def setChanged(): Unit =
      TunableChestBlockEntity.this.setChanged()


object TunableChestBlockEntity:
  val logger: Logger = LoggerFactory.getLogger(classOf[TunableChestBlockEntity])