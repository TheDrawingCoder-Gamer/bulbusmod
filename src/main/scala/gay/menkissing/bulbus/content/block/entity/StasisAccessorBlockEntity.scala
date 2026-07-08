package gay.menkissing.bulbus.content.block.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import gay.menkissing.bulbus.registries.BulbusBlockEntities
import it.unimi.dsi.fastutil.HashCommon
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer, BlockEntityRendererProvider}
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.item.{ItemModelResolver, ItemStackRenderState}
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.{BlockPos, HolderLookup}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.{ClientGamePacketListener, ClientboundBlockEntityDataPacket}
import net.minecraft.util.{Mth, ProblemReporter}
import net.minecraft.world.{Container, ContainerHelper}
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.item.{ItemDisplayContext, ItemStack}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.TagValueOutput
import net.minecraft.world.phys.Vec3
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Using

class StasisAccessorBlockEntity(pos: BlockPos, state: BlockState) extends StasisStorageBlockEntity(1, BulbusBlockEntities.stasisAccessor, pos, state):
  override val containerView: Container = new ContainerForStasisStorage
  override val containerStorage: ContainerStorage = ContainerStorage.of(containerView, null)

  def getStoredItem: ItemStack =
    items.get(0)

  override def setChanged(): Unit =
    super.setChanged()
    if this.level != null && !this.level.isClientSide then
      this.level.sendBlockUpdated(this.getBlockPos, this.getBlockState, this.getBlockState, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS)

  override def getUpdatePacket: Packet[ClientGamePacketListener] =
    ClientboundBlockEntityDataPacket.create(this)

  override def getUpdateTag(registries: HolderLookup.Provider): CompoundTag =
    Using.resource(ProblemReporter.ScopedCollector(this.problemPath(), StasisAccessorBlockEntity.Logger)): reporter =>
      val output = TagValueOutput.createWithContext(reporter, registries)
      ContainerHelper.saveAllItems(output, this.items)
      output.buildResult()
object StasisAccessorBlockEntity:
  val Logger: Logger = LoggerFactory.getLogger(classOf[StasisAccessorBlockEntity])

  @Environment(EnvType.CLIENT)
  final class StasisAccessorBlockEntityRenderState extends BlockEntityRenderState:
    var storedItem: Option[ItemStackRenderState] = None
    var spin: Float = 0.0f

    def clear(): Unit =
      storedItem = None
      spin = 0.0f
  @Environment(EnvType.CLIENT)
  final class StasisAccessorBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) extends BlockEntityRenderer[StasisAccessorBlockEntity, StasisAccessorBlockEntityRenderState]:
    private val itemModelResolver: ItemModelResolver = ctx.itemModelResolver()

    override def createRenderState(): StasisAccessorBlockEntityRenderState = new StasisAccessorBlockEntityRenderState

    override def extractRenderState(blockEntity: StasisAccessorBlockEntity, state: StasisAccessorBlockEntityRenderState, partialTicks: Float, cameraPosition: Vec3, breakProgress: ModelFeatureRenderer.CrumblingOverlay): Unit =
      super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress)
      val stack = blockEntity.getStoredItem
      if !stack.isEmpty then
        val itemStackRenderState = new ItemStackRenderState
        itemModelResolver.updateForTopItem(itemStackRenderState, stack, ItemDisplayContext.GROUND, blockEntity.getLevel, null, ItemClusterRenderState.getSeedForItemStack(stack))
        state.storedItem = Some(itemStackRenderState)
        // todo: spin
      else
        state.storedItem = None

    override def submit(state: StasisAccessorBlockEntityRenderState, poseStack: PoseStack, submitNodeCollector: SubmitNodeCollector, camera: CameraRenderState): Unit =
      state.storedItem.foreach: storedItem =>
        poseStack.pushPose()
        poseStack.translate(0.5f, 0.4f, 0.5f)
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spin))
        storedItem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0)

        poseStack.popPose()
