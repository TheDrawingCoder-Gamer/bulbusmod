package gay.menkissing.bulbus.client.content.itemmodels

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Transformation
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gay.menkissing.bulbus.content.item.StasisTubeItem
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.item.{ItemModel, ItemModelResolver, ItemStackRenderState}
import net.minecraft.client.resources.model.ResolvableModel
import net.minecraft.client.resources.model.cuboid.ItemTransform
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.{ItemDisplayContext, ItemStack}
import org.joml.Matrix4fc

import java.util.Optional

class TubeStoredItemSpecialRenderer(val transform: Matrix4fc) extends ItemModel:
  override def update(output: ItemStackRenderState, item: ItemStack, resolver: ItemModelResolver, displayContext: ItemDisplayContext, level: ClientLevel, owner: ItemOwner, seed: Int): Unit =
    output.appendModelIdentityElement(this)
    val ourLayer = output.newLayer()
    ourLayer.setLocalTransform(transform)
    StasisTubeItem.getStoredItem(item).foreach: variant =>
      val oldCount = output.layers.length
      resolver.appendItemLayers(output, variant.toStack, ItemDisplayContext.GUI, level, owner, seed)
      val newCount = output.layers.length
      if newCount > oldCount then
        val pose = new PoseStack
        pose.mulPose(transform)
        (oldCount until newCount).foreach: i =>
          val layer = output.layers(i)
          // Due to ordering, we need to extract the transform and apply it ourselves, but in the correct order
          pose.pushPose()
          layer.itemTransform.apply(false, pose.last())
          pose.mulPose(layer.localTransform)
          layer.localTransform.set(pose.last().pose())
          pose.popPose()
          // Then tell it that, no no there is no item transform
          layer.setItemTransform(ItemTransform.NO_TRANSFORM)
          

object TubeStoredItemSpecialRenderer:
  
  final case class Unbaked(transformation: Optional[Transformation]) extends ItemModel.Unbaked:
    override def `type`(): MapCodec[? <: ItemModel.Unbaked] = Unbaked.MAP_CODEC

    override def bake(context: ItemModel.BakingContext, transformation: Matrix4fc): ItemModel =
      val childTransform = Transformation.compose(transformation, this.transformation)
      new TubeStoredItemSpecialRenderer(childTransform)

    override def resolveDependencies(resolver: ResolvableModel.Resolver): Unit = ()

  object Unbaked:
    val MAP_CODEC: MapCodec[Unbaked] = RecordCodecBuilder.mapCodec[Unbaked](inst => inst.group(Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(_.transformation)).apply(inst, Unbaked.apply))
