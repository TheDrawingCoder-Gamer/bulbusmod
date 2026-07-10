package gay.menkissing.bulbus.client.content.itemmodels

import com.mojang.math.{Quadrant, Transformation}
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gay.menkissing.bulbus.client.content.color.item.BottleContentsTint
import gay.menkissing.bulbus.registries.BulbusDataComponentTypes
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidVariant, FluidVariantAttributes}
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.block.dispatch.{BlockModelRotation, ModelState}
import net.minecraft.client.renderer.chunk.ChunkSectionLayer
import net.minecraft.client.renderer.item.ItemModel.BakingContext
import net.minecraft.client.renderer.item.{CuboidItemModelWrapper, EmptyModel, ItemModel, ItemModelResolver, ItemStackRenderState, ModelRenderProperties}
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.resources.model.ResolvableModel
import net.minecraft.client.resources.model.cuboid.{CuboidFace, FaceBakery, ItemModelGenerator, ItemTransforms}
import net.minecraft.client.resources.model.geometry.{BakedQuad, QuadCollection}
import net.minecraft.client.resources.model.sprite.{Material, SpriteId}
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.{ItemDisplayContext, ItemStack}
import net.minecraft.world.level.material.{Fluid, Fluids}
import org.joml.{Matrix4fc, Vector3f, Vector4f, Vector4fc}

import java.util as ju
import scala.collection.mutable

// Renders a fluid, completely filling the slot
@Environment(EnvType.CLIENT)
class BottleFluidContentsModel(val transform: Matrix4fc, val crop: Vector4fc, val bakingContext: ItemModel.BakingContext) extends ItemModel:
  private val cache = mutable.HashMap.empty[Fluid, ItemModel]
  private def bakeModelForFluid(fluid: Fluid): ItemModel =
    // ???
    val baker = bakingContext.blockModelBaker()
    val materials = baker.materials()
    val fluidModel = Minecraft.getInstance()
                              .getModelManager
                              .getFluidStateModelSet
                              .get(fluid.defaultFluidState())

    val badFluidSprite = if fluid != Fluids.EMPTY then fluidModel.stillMaterial() else null
    val fluidSprite =
      if badFluidSprite == null then
        val eagerFluidName = BuiltInRegistries.FLUID.getKey(fluid).toString
        materials.reportMissingReference("fluid_sprite", () => eagerFluidName)
      else
        badFluidSprite
    val renderProperties = new ModelRenderProperties(false, fluidSprite, ItemTransforms.NO_TRANSFORMS)

    val interner = baker.interner()
    val fluidInfo = interner.materialInfo(new BakedQuad.MaterialInfo(
        fluidSprite.sprite(), ChunkSectionLayer.CUTOUT, BottleFluidContentsModel.computeFluidItemRenderType(fluidSprite),
      0, false, 0
    ))

    val builder = new QuadCollection.Builder
    
    val from = Vector3f(crop.x(), crop.y(), 8.5f)
    val to = Vector3f(crop.z(), crop.w(), 9.5f)
    
    builder.addUnculledFace(FaceBakery.bakeQuad(interner, from, to, BottleFluidContentsModel.croppedFace(crop), Quadrant.R0, fluidInfo, Direction.SOUTH, BlockModelRotation.IDENTITY, null))
    val quads = builder.build()

    new CuboidItemModelWrapper(ju.List.of(BottleContentsTint), quads, renderProperties,  transform)







  override def update(output: ItemStackRenderState, item: ItemStack, resolver: ItemModelResolver, displayContext: ItemDisplayContext, level: ClientLevel, owner: ItemOwner, seed: Int): Unit =
    val contents = item.get(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS)
    if contents != null && !contents.isEmpty then
      val fluid = contents.variant.getFluid
      cache.getOrElseUpdate(fluid, bakeModelForFluid(fluid))
           .update(output, item, resolver, displayContext, level, owner, seed)


@Environment(EnvType.CLIENT)
object BottleFluidContentsModel:
  val DEBUG_NAME = "BottleFluidContentsModel"
  
  def croppedFace(crop: Vector4fc): CuboidFace.UVs =
    CuboidFace.UVs(crop.x(), crop.y(), crop.z(), crop.w())

  def computeFluidItemRenderType(material: Material.Baked): RenderType =
    val translucent = material.forceTranslucent() || material.sprite().transparency().hasTranslucent
    // val translucent = false
    if material.sprite().atlasLocation() == TextureAtlas.LOCATION_BLOCKS then
      if translucent then
        Sheets.translucentBlockItemSheet()
      else
        Sheets.cutoutBlockItemSheet()
    else
      if translucent then
        Sheets.translucentItemSheet()
      else
        Sheets.cutoutItemSheet()

  @Environment(EnvType.CLIENT)
  final case class Unbaked(transformation: ju.Optional[Transformation], crop: Vector4fc) extends ItemModel.Unbaked:
    override def `type`(): MapCodec[? <: ItemModel.Unbaked] = Unbaked.MAP_CODEC

    override def bake(context: ItemModel.BakingContext, transformation: Matrix4fc): ItemModel =
      val childTransform = Transformation.compose(transformation, this.transformation)
      BottleFluidContentsModel(childTransform, crop, context)

    override def resolveDependencies(resolver: ResolvableModel.Resolver): Unit =()

  @Environment(EnvType.CLIENT)
  object Unbaked:
    val MAP_CODEC: MapCodec[Unbaked] = RecordCodecBuilder.mapCodec(inst =>
      inst.group(
        Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(_.transformation),
        ExtraCodecs.VECTOR4F.optionalFieldOf("crop", Vector4f(0f, 0f, 16f, 16f)).forGetter(_.crop)
      ).apply(inst, Unbaked.apply)
    )