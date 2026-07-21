package gay.menkissing.bulbus.content.item

import net.minecraft.world.item.ProjectileItem
import net.minecraft.world.item.Item
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.core.Position
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ProjectileItem.DispenseConfig
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.server.level.ServerLevel
import net.minecraft.stats.Stats
import gay.menkissing.bulbus.content.entity.ThrownRepairBottle

class RepairBottleItem
  (props: Item.Properties)
    extends Item(props),
      ProjectileItem:

  override def use(level: Level, player: Player, hand: InteractionHand): InteractionResult =
    val stack = player.getItemInHand(hand)
    level.playSound(
      null,
      player.getX,
      player.getY,
      player.getZ,
      SoundEvents.EXPERIENCE_BOTTLE_THROW,
      SoundSource.NEUTRAL,
      0.5f,
      0.4f / (level.getRandom.nextFloat() * 0.4f + 0.8f)
    )
    level match
      case serverLevel: ServerLevel =>
        Projectile.spawnProjectileFromRotation(ThrownRepairBottle.fromOwner, serverLevel, stack, player, -20f, 0.7f, 1.0f)
      case _ => ()
    
    player.awardStat(Stats.ITEM_USED.get(this))
    stack.consume(1, player)

    InteractionResult.SUCCESS
  override def asProjectile
    (
      level: Level,
      position: Position,
      itemStack: ItemStack,
      direction: Direction
    ): Projectile = ???

  override def createDispenseConfig(): DispenseConfig =
    DispenseConfig.builder()
      .uncertainty(DispenseConfig.DEFAULT.uncertainty() * 0.5f)
      .power(DispenseConfig.DEFAULT.power() * 1.25f).build()
