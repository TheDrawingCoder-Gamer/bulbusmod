package gay.menkissing.bulbus.screen.slots

import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class LockedSlot(container: Container, slot: Int, x: Int, y: Int) extends Slot(container, slot, x, y):
  override def mayPickup(player: Player): Boolean = false

  override def mayPlace(itemStack: ItemStack): Boolean = false

