package gay.menkissing.bulbus.content.entity

import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import gay.menkissing.bulbus.registries.BulbusEntities
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.syncher.SynchedEntityData.Builder
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.InterpolationHandler
import net.minecraft.world.entity.player.Player
import net.minecraft.tags.FluidTags
import net.minecraft.world.phys.Vec3
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.enchantment.EnchantedItemInUse
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.MoverType
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.AABB
import net.minecraft.world.entity.Entity.MovementEmission
import net.minecraft.world.entity.EntityType

class RepairOrb(tpe: EntityType[? <: RepairOrb], level: Level) extends Entity(tpe, level):
  private var age: Int = 0
  private var health: Int = 5
  private val interpolation = new InterpolationHandler(this)
  private var followingPlayer: Option[Player] = None
  

  private def unstuckIfPossible(maxDistance: Double): Unit =
    val center = this.position().add(0, this.getBbHeight() / 2.0, 0)
    val allowedCenters = Shapes.create(AABB.ofSize(center, maxDistance, maxDistance, maxDistance))
    this.level
      .findFreePosition(this, allowedCenters, center, this.getBbWidth(), this.getBbHeight(), this.getBbWidth())
      .ifPresent(pos => this.setPos(pos.add(0, -this.getBbHeight() / 2.0, 0)))

  override protected def addAdditionalSaveData(output: ValueOutput): Unit =
    output.putShort("Health", this.health.toShort)
    output.putShort("Age", this.age.toShort)

  override protected def readAdditionalSaveData(input: ValueInput): Unit =
    this.health = input.getShortOr("Health", 5)
    this.age = input.getShortOr("Age", 0)
  
  override protected def getMovementEmission(): MovementEmission = MovementEmission.NONE

  override def hurtClient(source: DamageSource): Boolean =
    !this.isInvulnerableToBase(source)

  override def hurtServer
    (level: ServerLevel, source: DamageSource, damage: Float): Boolean =
    if this.isInvulnerableToBase(source) then false
    else
      this.markHurt()
      this.health = (this.health - damage).toInt
      if this.health <= 0 then this.discard()

      true
  override protected def defineSynchedData
    (entityData: SynchedEntityData.Builder): Unit =
    // entityData.define(RepairOrb.DATA_VALUE, 0)
    ()

  override protected def doWaterSplashEffect(): Unit = ()

  override def getSoundSource(): SoundSource = SoundSource.AMBIENT

  override def getInterpolation(): InterpolationHandler = interpolation

  override protected def getDefaultGravity(): Double = 0.03

  private def makeSoggy(): Unit =
    val movement = getDeltaMovement
    this.setDeltaMovement(
      movement.x * 0.99f,
      math.min(movement.y + 5.0e-4f, 0.06f),
      movement.z * 0.99f
    )
  override def tick(): Unit =
    this.interpolation.interpolate()
    if this.firstTick && this.level.isClientSide() then this.firstTick = false
    else
      super.tick()
      val colliding = !this.level.noCollision(this.getBoundingBox())
      if this.isEyeInFluid(FluidTags.WATER) then this.makeSoggy()
      else if !colliding then this.applyGravity()

      if level.getFluidState(this.blockPosition()).is(FluidTags.LAVA) then
        this.setDeltaMovement(
          (this.random.nextFloat() - this.random.nextFloat()) * 0.2f,
          0.2f,
          (this.random.nextFloat() - this.random.nextFloat()) * 0.2f
        )
      
      this.stalkNearbyChildren()
      if this.followingPlayer.isEmpty && !this.level.isClientSide() && colliding then
        val nextColliding = !this.level.noCollision(this.getBoundingBox().move(this.getDeltaMovement()))
        if nextColliding then
          this.moveTowardsClosestSpace(this.getX, (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ)
          this.needsSync = true
      
      val fallSpeed = this.getDeltaMovement().y
      this.move(MoverType.SELF, this.getDeltaMovement())
      this.applyEffectsFromBlocks()
      val friction = 
        if this.onGround then
          this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction()
        else 0.98f
      
      this.setDeltaMovement(this.getDeltaMovement().scale(friction))
      if this.verticalCollisionBelow && fallSpeed < -this.getGravity() then
        this.setDeltaMovement(Vec3(this.getDeltaMovement().x, -fallSpeed * 0.4f, this.getDeltaMovement().z))
      
      this.age += 1
      if this.age >= 6000 then
        this.discard()

  // ???
  private def stalkNearbyChildren(): Unit =
    if this.followingPlayer.isEmpty ||
      this.followingPlayer
        .forall(it => it.isSpectator || it.distanceToSqr(this) > 64f)
    then
      val nearestPlayer: Player | Null = this.level.getNearestPlayer(this, 8.0)
      nearestPlayer match
        case null => followingPlayer = None
        case it if !it.isSpectator && !it.isDeadOrDying =>
          followingPlayer = Some(it)
        case _ => followingPlayer = None

    followingPlayer.foreach: following =>
      val delta =
        Vec3(
          following.getX - this.getX,
          following.getY + following.getEyeHeight() / 2.0 - this.getY,
          following.getZ - this.getZ
        )
      val length = delta.lengthSqr()
      val power = 1.0 - math.sqrt(length) / 8.0
      this.setDeltaMovement(
        getDeltaMovement.add(delta.normalize().scale(power * power * 0.1))
      )

  override def getBlockPosBelowThatAffectsMyMovement(): BlockPos =
    getOnPos(0.999999f)

  override def isAttackable(): Boolean = false

  override def playerTouch(player: Player): Unit =
    player match
      case serverPlayer: ServerPlayer =>
        if player.takeXpDelay == 0 then
          player.takeXpDelay = 2
          // TODO: visuals for da client : )
          // player.take
          val remaining = this.repairPlayerItems(serverPlayer)

          this.discard()

  @annotation.tailrec
  private def repairPlayerItems(player: ServerPlayer, amount: Int = RepairOrb.eachOrbRepairs): Int =
    RepairOrb.getRandomDamagedItem(player) match
      case Some(selected) =>
        val stack = selected.itemStack()
        val toRepair = math.min(amount, stack.getDamageValue)
        stack.setDamageValue(stack.getDamageValue - toRepair)
        if toRepair > 0 then
          val remaining = amount - toRepair
          if remaining > 0 then
            this.repairPlayerItems(player, remaining)
          else 0
        else 0
      case None => amount

object RepairOrb:
  final val eachOrbRepairs: Int = 50

  //val DATA_VALUE: EntityDataAccessor[Integer] =
  //  SynchedEntityData.defineId(classOf[RepairOrb], EntityDataSerializers.INT);

  def getRandomDamagedItem(source: LivingEntity): Option[EnchantedItemInUse] =
    val items = List.newBuilder[EnchantedItemInUse]

    EquipmentSlot.VALUES.forEach: slot =>
      val item = source.getItemBySlot(slot)
      if item.isDamaged() then
        items += EnchantedItemInUse(item, slot, source)
    
    items.result() match
      case Nil => None
      case ls =>
        Some(ls(source.getRandom.nextInt(ls.size)))

  def apply(tpe: EntityType[? <: RepairOrb], level: Level): RepairOrb =
    new RepairOrb(tpe, level)

  def apply(level: Level, x: Double, y: Double, z: Double): RepairOrb =
    apply(level, Vec3(x, y, z), Vec3.ZERO)
  
  def apply(level: Level, pos: Vec3, roughly: Vec3): RepairOrb =
    val res = new RepairOrb(BulbusEntities.repairOrb, level)
    res.setPos(pos)
    if !level.isClientSide() then
      res.setYRot(res.getRandom().nextFloat() * 360.0f)
      var randomMovement = new Vec3((res.getRandom().nextDouble() * 0.2 - 0.1) * 2.0, res.getRandom().nextDouble() * 0.2 * 2.0, (res.getRandom().nextDouble() * 0.2 - 0.1) * 2.0)
      if roughly.lengthSqr > 0.0 && roughly.dot(randomMovement) < 0.0 then
        randomMovement = randomMovement.scale(-1.0)

      val size = res.getBoundingBox().getSize()
      res.setPos(pos.add(roughly.normalize().scale(size * 0.5)))
      res.setDeltaMovement(randomMovement)
      if !level.noCollision(res.getBoundingBox()) then
        res.unstuckIfPossible(size)
    

    res
  
  def award(level: ServerLevel, pos: Vec3, roughDirection: Vec3 = Vec3.ZERO): Unit =
    level.addFreshEntity(RepairOrb(level, pos, roughDirection))