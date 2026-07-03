package gay.menkissing.bulbus.util

import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidVariant, FluidVariantAttributes}
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.sounds.{SoundEvents, SoundSource}
import net.minecraft.tags.FluidTags
import net.minecraft.world.attribute.EnvironmentAttributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.{Block, LiquidBlockContainer}
import net.minecraft.world.level.material.{FlowingFluid, Fluid, Fluids}
import net.minecraft.world.phys.BlockHitResult

import scala.annotation.tailrec
import scala.util.Using

object FluidUtil:
  def fluidMayBePlaced(fluid: Fluid): Boolean =
    fluid.isInstanceOf[FlowingFluid]


  def playEmptyingSound(player: Player, level: Level, pos: BlockPos, variant: FluidVariant): Unit =
    val sound = FluidVariantAttributes.getEmptySound(variant)
    if sound != null then
      level.playSound(player, pos, sound, SoundSource.BLOCKS, 1f, 1f)


  // V IDE complains but I GENUINELY can manage 1 stack by myself thank u very much
  @tailrec
  def placeFluid(player: Player, level: Level, pos: BlockPos, hitResult: BlockHitResult | Null, variant: FluidVariant): Boolean =
    if variant.isBlank || !fluidMayBePlaced(variant.getFluid) then
      false
    else
      val flowingFluid =variant.getFluid.asInstanceOf[FlowingFluid]
      val destBlockState = level.getBlockState(pos)
      val isDestReplaceable = destBlockState.canBeReplaced(variant.getFluid)
      val canDestContainFluid =
        destBlockState.getBlock match
          case lbc: LiquidBlockContainer => lbc.canPlaceLiquid(player, level, pos, destBlockState, variant.getFluid)
          case _ => false

      if !destBlockState.isAir && !isDestReplaceable && !canDestContainFluid then
        hitResult != null && placeFluid(player, level, hitResult.getBlockPos.relative(hitResult.getDirection), null, variant)
      else if level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos).booleanValue() && variant.getFluid.is(FluidTags.WATER) then
        val x = pos.getX
        val y = pos.getY
        val z = pos.getZ
        val random = level.getRandom
        level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f)

        (0 until 8).foreach: i =>
          level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0, 0.0, 0.0)

        true
      // Scuffed AF
      else if canDestContainFluid then
        val lbc = destBlockState.getBlock.asInstanceOf[LiquidBlockContainer]
        lbc.placeLiquid(level, pos, destBlockState, flowingFluid.getSource(false))
        playEmptyingSound(player, level, pos, variant)
        true
      else
        if !level.isClientSide && isDestReplaceable && !destBlockState.liquid() then
          level.removeBlock(pos, true)

        if level.setBlock(pos, flowingFluid.getSource(false).createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE) then
          playEmptyingSound(player, level, pos, variant)
          true
        else
          false


