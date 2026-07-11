package gay.menkissing.bulbus.client.content.renderers

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import gay.menkissing.bulbus.content.block.entity.RepairMachineBlockEntity
import gay.menkissing.bulbus.client.content.renderers.state.RepairMachineRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import org.joml.Quaternionf
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay
import net.minecraft.world.phys.Vec3
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState
import net.minecraft.util.Mth

class RepairMachineRenderer(ctx: BlockEntityRendererProvider.Context)
    extends BlockEntityRenderer[
      RepairMachineBlockEntity,
      RepairMachineRenderState
    ]:

  private val itemModelResolver: ItemModelResolver = ctx.itemModelResolver()

  override def extractRenderState(blockEntity: RepairMachineBlockEntity, state: RepairMachineRenderState, partialTicks: Float, cameraPosition: Vec3, breakProgress: CrumblingOverlay): Unit = 
    super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress)
    state.ageInTicks = blockEntity.ageInTicks.toFloat + partialTicks
    state.active = blockEntity.active
    if !blockEntity.heldGem.isEmpty then
      val itemState = new ItemStackRenderState
      itemModelResolver.updateForTopItem(itemState, blockEntity.heldGem, ItemDisplayContext.GROUND, blockEntity.getLevel, null, ItemClusterRenderState.getSeedForItemStack(blockEntity.heldGem))
      state.gem = Some(itemState)
    if !blockEntity.primaryItem.isEmpty then
      val itemState = new ItemStackRenderState
      itemModelResolver.updateForTopItem(itemState, blockEntity.primaryItem, ItemDisplayContext.GROUND, blockEntity.getLevel, null, ItemClusterRenderState.getSeedForItemStack(blockEntity.primaryItem))
      state.primaryItem = Some(itemState)

  override def submit
    (
      state: RepairMachineRenderState,
      poseStack: PoseStack,
      submitNodeCollector: SubmitNodeCollector,
      camera: CameraRenderState
    ): Unit =
    state.gem.foreach: gem =>
      poseStack.pushPose()
      val ourOrientation = new Quaternionf(camera.orientation)
      poseStack.translate(0.5f, 2.5f, 0.5f)
      poseStack.mulPose(ourOrientation)

      if state.active then
        val scale = Mth.sin(state.ageInTicks / 20.0f) * 0.1f + 1f
        poseStack.scale(scale, scale, scale)

      gem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0)

      poseStack.popPose()
    
    state.primaryItem.foreach: primaryItem =>
      poseStack.pushPose()
      poseStack.translate(0.5f, 1.1f, 0.5f)

      primaryItem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0)


      poseStack.popPose()

  override def createRenderState(): RepairMachineRenderState =
    new RepairMachineRenderState
