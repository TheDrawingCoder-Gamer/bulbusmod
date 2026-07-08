package gay.menkissing.bulbus.content.block.entity

import gay.menkissing.bulbus.registries.BulbusBlockEntities
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.level.block.state.BlockState

// not really anything special here...
class StasisAccessorBlockEntity(pos: BlockPos, state: BlockState) extends StasisStorageBlockEntity(1, BulbusBlockEntities.stasisAccessor, pos, state):
  override val containerView: Container = new ContainerForStasisStorage
  override val containerStorage: ContainerStorage = ContainerStorage.of(containerView, null)
