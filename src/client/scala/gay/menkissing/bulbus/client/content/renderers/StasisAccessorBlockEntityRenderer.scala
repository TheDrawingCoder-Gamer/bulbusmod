package gay.menkissing.bulbus.client.content.renderers

import state.StasisAccessorBlockEntityRenderState
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import gay.menkissing.bulbus.content.block.entity.StasisAccessorBlockEntity
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer, BlockEntityRendererProvider}
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.item.{ItemModelResolver, ItemStackRenderState}
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.phys.Vec3

final class StasisAccessorBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) extends BlockEntityRenderer[StasisAccessorBlockEntity, StasisAccessorBlockEntityRenderState]:
  private val itemModelResolver: ItemModelResolver = ctx.itemModelResolver()

  override def createRenderState(): StasisAccessorBlockEntityRenderState = new StasisAccessorBlockEntityRenderState

  override def extractRenderState(blockEntity: StasisAccessorBlockEntity, state: StasisAccessorBlockEntityRenderState, partialTicks: Float, cameraPosition: Vec3, breakProgress: ModelFeatureRenderer.CrumblingOverlay): Unit =
    super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress)
    val stack = blockEntity.getStoredItem
    state.ageInTicks = blockEntity.ageInTicks.toFloat + partialTicks
    if !stack.isEmpty then
      val itemStackRenderState = new ItemStackRenderState
      itemModelResolver.updateForTopItem(itemStackRenderState, stack, ItemDisplayContext.GROUND, blockEntity
        .getLevel, null, ItemClusterRenderState.getSeedForItemStack(stack))
      state.storedItem = Some(itemStackRenderState)
    else
      state.storedItem = None

  override def submit(state: StasisAccessorBlockEntityRenderState, poseStack: PoseStack, submitNodeCollector: SubmitNodeCollector, camera: CameraRenderState): Unit =
    state.storedItem.foreach: storedItem =>
      poseStack.pushPose()
      val boundingBox = storedItem.getModelBoundingBox
      val minOffsetY = -boundingBox.minY.toFloat + StasisAccessorBlockEntityRenderer.itemMinHoverHeight
      // why does minecraft have its own sin??????
      val bob = Mth.sin(state.ageInTicks / 10.0f + minOffsetY) * 0.05f + 0.05f

      poseStack.translate(0.5f, 0.35f + bob, 0.5f)
      val spin = ItemEntity.getSpin(state.ageInTicks, 0f)
      poseStack.mulPose(Axis.YP.rotation(spin))
      storedItem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0)

      poseStack.popPose()

object StasisAccessorBlockEntityRenderer:
  final val itemMinHoverHeight: Float = 0.0625f
