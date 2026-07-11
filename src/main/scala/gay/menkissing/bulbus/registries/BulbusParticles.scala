package gay.menkissing.bulbus.registries

import net.minecraft.core.particles.SimpleParticleType
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import gay.menkissing.bulbus.BulbusMod

object BulbusParticles:
  val repairParticle: SimpleParticleType = FabricParticleTypes.simple()

  def init(): Unit =
    Registry.register(
      BuiltInRegistries.PARTICLE_TYPE,
      BulbusMod.locate("repair_particle"),
      repairParticle
    )
