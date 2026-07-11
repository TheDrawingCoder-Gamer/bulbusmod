package gay.menkissing.bulbus.content.item

import gay.menkissing.bulbus.api.XPStorage
import gay.menkissing.bulbus.util.BulbusEnchantmentUtil
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.core.{BlockPos, Holder, HolderLookup}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.{SoundEvents, SoundSource}
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.enchantment.{Enchantment, Enchantments}
import net.minecraft.world.item.{Item, ItemStack, ItemUseAnimation, ItemUtils}
import net.minecraft.world.level.Level

import scala.util.Using

class KnowledgeStorageItem(props: Item.Properties) extends Item(props):

  override def allowComponentsUpdateAnimation(player: Player, hand: InteractionHand, oldStack: ItemStack, newStack: ItemStack): Boolean =
    false

  override def canBeEnchantedWith(stack: ItemStack, enchantment: Holder[Enchantment], context: EnchantingContext): Boolean =
    super.canBeEnchantedWith(stack, enchantment, context) || enchantment.is(Enchantments.EFFICIENCY) ||
      enchantment.is(Enchantments.QUICK_CHARGE)

  override def use(level: Level, player: Player, hand: InteractionHand): InteractionResult =
    ItemUtils.startUsingInstantly(level, player, hand)

  override def getUseAnimation(itemStack: ItemStack): ItemUseAnimation =
    ItemUseAnimation.BOW

  // max value that floating point numbers can do every 0.5f for, so animation doesnt look completely broken
  override def getUseDuration(itemStack: ItemStack, user: LivingEntity): Int = 8_388_608

  def tryPlayUseSound(level: Level, user: LivingEntity, remainingUseTicks: Int): Unit =
    if remainingUseTicks % 4 == 0 then
      level.playSound(null, user.getX, user.getY, user.getZ, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.8f + level.getRandom.nextFloat() * 0.4f)

  override def onUseTick(level: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int): Unit =
    super.onUseTick(level, user, stack, remainingUseTicks)
    user match
      case player: ServerPlayer =>
        val hand =
          if player.getItemInHand(InteractionHand.MAIN_HAND) eq stack then
            InteractionHand.MAIN_HAND
          else
            InteractionHand.OFF_HAND
        val storage = ContainerItemContext.ofPlayerHand(player, hand).find(XPStorage.ITEM)
        if storage != null then
          val available = player.totalExperience
          val rate = KnowledgeStorageItem.getTransferRate(level.registryAccess(), stack)

          if player.isShiftKeyDown then
            val offer = if player.isCreative then rate else math.min(rate, available)
            Using.resource(Transaction.openOuter()): trans =>
              val inserted = storage.insert(offer, trans)
              if inserted != 0 then
                KnowledgeStorageItem.removePlayerXp(player, inserted.toInt)
                tryPlayUseSound(level, user, remainingUseTicks)
                trans.commit()
          else
            Using.resource(Transaction.openOuter()): trans =>
              val drain = storage.extract(rate, trans)
              if drain != 0 then
                player.giveExperiencePoints(drain.toInt)
                if !player.isCreative then
                  trans.commit()
                tryPlayUseSound(level, user, remainingUseTicks)

      case _ => ()

object KnowledgeStorageItem:
  val baseMax: Long = 10_000

  def removePlayerXp(player: Player, xp: Int): Boolean =
    if player.isCreative then
      true
    else if player.totalExperience < xp then
      false
    else
      player.giveExperiencePoints(-xp)
      true

  def getTransferRate(lookup: HolderLookup.Provider, stack: ItemStack): Long =
    val quickCharge = BulbusEnchantmentUtil.getLevel(lookup, Enchantments.QUICK_CHARGE, stack)
    (2 * math.pow(2, math.min(10, quickCharge))).toInt


  def getMax(level: Int): Long =
    baseMax * math.pow(10, math.min(5, level)).toInt

  def getMax(lookup: HolderLookup.Provider, stack: DataComponentGetter): Long =
    val efficiencyLevel = BulbusEnchantmentUtil.getLevel(lookup, Enchantments.EFFICIENCY, stack)
    getMax(efficiencyLevel)

  def getMaxEvil(stack: DataComponentGetter): Long =
    val efficiencyLevel = BulbusEnchantmentUtil.getLevelEvil(Enchantments.EFFICIENCY, stack)
    getMax(efficiencyLevel)

