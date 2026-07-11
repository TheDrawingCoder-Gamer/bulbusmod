package gay.menkissing.bulbus.client.content

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry
import gay.menkissing.bulbus.registries.BulbusParticles
import net.minecraft.client.particle.EndRodParticle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.core.particles.SimpleParticleType
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.PendingParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.GlowParticle
import net.minecraft.client.particle.SuspendedTownParticle

object BulbusClientParticles:
  def init(): Unit =
    ParticleProviderRegistry.getInstance()
      .register(BulbusParticles.repairParticle, stupidFuckingWrapperThatIHate(SuspendedTownParticle.HappyVillagerProvider.apply))

  def stupidFuckingWrapperThatIHate(f: SpriteSet => ParticleProvider[SimpleParticleType]): PendingParticleProvider[SimpleParticleType] =
    (fabricSet) => f(fabricSet)
