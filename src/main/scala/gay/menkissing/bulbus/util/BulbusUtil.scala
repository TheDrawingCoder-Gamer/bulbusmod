package gay.menkissing.bulbus.util

import net.minecraft.world.level.Level
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.Containers

object BulbusUtil:
  def giveOrDrop(level: Level, pos: BlockPos, player: Player, stack: ItemStack): Unit =
    if !player.addItem(stack) then
      Containers.dropItemStack(level, pos.getX, pos.getY + 1.2f, pos.getZ, stack)

