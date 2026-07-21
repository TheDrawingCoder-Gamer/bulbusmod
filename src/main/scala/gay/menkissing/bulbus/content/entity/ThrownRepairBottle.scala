package gay.menkissing.bulbus.content.entity

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.ItemStack
import gay.menkissing.bulbus.registries.BulbusEntities
import net.minecraft.world.level.Level
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import gay.menkissing.bulbus.registries.BulbusItems
import net.minecraft.world.phys.HitResult
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.entity.EntityType

class ThrownRepairBottle(tpe: EntityType[? <: ThrownRepairBottle], level: Level) extends ThrowableItemProjectile(tpe, level) {

  override protected def getDefaultItem: Item = BulbusItems.repairBottle

  override protected def getDefaultGravity(): Double = 0.07

  override protected def onHit(hitResult: HitResult): Unit =
    super.onHit(hitResult)
    this.level match
      case level: ServerLevel =>
        // data is color (this took 10 minutes to figure out)
        level.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, this.blockPosition(), 0xff2684c7)
        hitResult match
          case blockHitResult: BlockHitResult =>
            val blockNormalHit = blockHitResult.getDirection().getUnitVec3()
            RepairOrb.award(level, hitResult.getLocation(), blockNormalHit)
          case _ =>
            RepairOrb.award(level, hitResult.getLocation(), this.getDeltaMovement().scale(-1.0))

        this.discard()
      case _ => ()

  def this(level: Level, x: Double, y: Double, z: Double, stack: ItemStack) =
    this(BulbusEntities.thrownRepairBottle, level)
    this.setPos(x, y, z)
    this.setItem(stack)

  def this(level: Level, mob: LivingEntity, stack: ItemStack) =
    // > : (
    this(level, mob.getX, mob.getEyeY - 0.1f, mob.getZ, stack)
    this.setOwner(mob)
  
}

object ThrownRepairBottle:
  def create(tpe: EntityType[? <: ThrownRepairBottle], level: Level): ThrownRepairBottle =
    new ThrownRepairBottle(tpe, level)

  def fromOwner(level: Level, owner: LivingEntity, stack: ItemStack): ThrownRepairBottle =
    new ThrownRepairBottle(level, owner, stack)