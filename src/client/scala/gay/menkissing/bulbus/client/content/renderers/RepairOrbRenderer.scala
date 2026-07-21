package gay.menkissing.bulbus.client.content.renderers

import net.minecraft.client.renderer.entity.EntityRenderer
import gay.menkissing.bulbus.content.entity.RepairOrb
import net.minecraft.client.renderer.entity.EntityRendererProvider
import gay.menkissing.bulbus.client.content.renderers.state.RepairOrbRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.resources.Identifier
import gay.menkissing.bulbus.BulbusMod
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import com.mojang.blaze3d.vertex.PoseStack.Pose
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.texture.OverlayTexture

class RepairOrbRenderer(ctx: EntityRendererProvider.Context) extends EntityRenderer[RepairOrb, RepairOrbRenderState](ctx) {

  override def createRenderState(): RepairOrbRenderState = new RepairOrbRenderState

  override def submit(state: RepairOrbRenderState, poseStack: PoseStack, submitNodeCollector: SubmitNodeCollector, camera: CameraRenderState): Unit =
    import RepairOrbRenderer.*
    poseStack.pushPose()
    poseStack.translate(0.0f, 0.1f, 0.0f)
    poseStack.mulPose(camera.orientation)
    poseStack.scale(0.3f, 0.3f, 0.3f)
    submitNodeCollector.submitCustomGeometry(poseStack, RepairOrbRenderer.renderType, (pose, buffer) =>
      withCustomGeo(pose, buffer, state.lightCoords):
        vertex(-0.5f, -0.25f, 0, 1)
        vertex(0.5f, -0.25f, 1, 1)
        vertex(0.5f, 0.75f, 1, 0)
        vertex(-0.5f, 0.75f, 0, 0)
    )
    poseStack.popPose()
    super.submit(state, poseStack, submitNodeCollector, camera)

}

object RepairOrbRenderer:
  val texLocation: Identifier = BulbusMod.locate("textures/entity/repair_orb.png")
  val renderType: RenderType = RenderTypes.entityTranslucentCullItemTarget(texLocation)

  opaque type LightCoords = Int
  object LightCoords:
    def apply(v: Int): LightCoords = v

  def withCustomGeo(pose: Pose, buffer: VertexConsumer, coords: Int)(f: (Pose, VertexConsumer, LightCoords) ?=> Unit): Unit =
    f(using pose, buffer, LightCoords(coords))

  def vertex(x: Float, y: Float, u: Float, v: Float)(using pose: Pose, buffer: VertexConsumer, coords: LightCoords): Unit =
    buffer.addVertex(pose, x, y, 0.0f)
    .setColor(255, 255, 255, 128)
    .setUv(u, v)
    .setOverlay(OverlayTexture.NO_OVERLAY)
    .setLight(coords)
    .setNormal(pose, 0.0f, 1.0f, 0.0f)