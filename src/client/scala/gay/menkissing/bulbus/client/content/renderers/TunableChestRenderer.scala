package gay.menkissing.bulbus.client.content.renderers

import com.mojang.blaze3d.vertex.PoseStack
import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.client.content.models.TunableChestModel
import gay.menkissing.bulbus.client.content.renderers.state.TunableChestRenderState
import gay.menkissing.bulbus.content.block.entity.TunableChestBlockEntity
import net.minecraft.client.model.geom.{ModelLayerLocation, ModelLayers, ModelPart}
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer, BlockEntityRendererProvider}
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.rendertype.{RenderType, RenderTypes}
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.{OverlayTexture, TextureAtlas}
import net.minecraft.client.resources.model.sprite.SpriteId
import net.minecraft.resources.Identifier
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class TunableChestRenderer(ctx: BlockEntityRendererProvider.Context) extends BlockEntityRenderer[TunableChestBlockEntity, TunableChestRenderState]:
  private val button: TunableChestModel = new TunableChestModel(TunableChestModel.createLayer().bakeRoot())
  private val sprites = ctx.sprites()



  override def createRenderState(): TunableChestRenderState = new TunableChestRenderState

  override def extractRenderState(blockEntity: TunableChestBlockEntity, state: TunableChestRenderState, partialTicks: Float, cameraPosition: Vec3, breakProgress: ModelFeatureRenderer.CrumblingOverlay): Unit =
    super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress)
    state.channel = blockEntity.channel



  override def submit(state: TunableChestRenderState, poseStack: PoseStack, submitNodeCollector: SubmitNodeCollector, camera: CameraRenderState): Unit =
    poseStack.pushPose()
    poseStack.translate(0, 15 / 16f, 6f / 16f)


    def submitButton(color: DyeColor, px: Int): Unit =
      poseStack.pushPose()
      poseStack.translate(px.toFloat / 16, 0, 0)

      val spriteId = SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/" + color.getSerializedName + "_wool"))
      val sprite = sprites.get(spriteId)

      submitNodeCollector.submitModel(button, (), poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, spriteId, this.sprites, 0, null)

      poseStack.popPose()

    submitButton(state.channel.first, 4)
    submitButton(state.channel.second, 7)
    submitButton(state.channel.third, 10)





    poseStack.popPose()



object TunableChestRenderer:
  val ids: Map[DyeColor, ModelLayerLocation] =
    DyeColor.values().map: color =>
      color -> ModelLayerLocation(BulbusMod.locate("block/tunable_chest_button_" + color.getSerializedName), "main")
    .toMap
