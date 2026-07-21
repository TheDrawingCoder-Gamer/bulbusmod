package gay.menkissing.bulbus.client.content

import net.minecraft.client.renderer.entity.EntityRenderers
import gay.menkissing.bulbus.registries.BulbusEntities
import gay.menkissing.bulbus.client.content.renderers.RepairOrbRenderer
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider

object BulbusEntityRenderers:
  def register(): Unit =
    EntityRenderers.register(BulbusEntities.repairOrb, RepairOrbRenderer.apply)
    EntityRenderers.register(BulbusEntities.thrownRepairBottle, (ctx: EntityRendererProvider.Context) => ThrownItemRenderer(ctx))
