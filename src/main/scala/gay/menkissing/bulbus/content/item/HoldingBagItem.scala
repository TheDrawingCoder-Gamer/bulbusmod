package gay.menkissing.bulbus.content.item

import gay.menkissing.bulbus.screen.HoldingBagMenu
import net.minecraft.world.{InteractionHand, InteractionResult, SimpleMenuProvider}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level

class HoldingBagItem(props: Item.Properties) extends Item(props):
  override def use(level: Level, player: Player, hand: InteractionHand): InteractionResult =
    if !level.isClientSide then
      val stack = player.getItemInHand(hand)
      player.openMenu(new SimpleMenuProvider((syncId, inventory, player2) => HoldingBagMenu(syncId, inventory, player2.getEnderChestInventory), stack.getHoverName))
    InteractionResult.SUCCESS
