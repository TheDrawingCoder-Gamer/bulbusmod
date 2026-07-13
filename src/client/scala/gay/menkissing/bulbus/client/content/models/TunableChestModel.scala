package gay.menkissing.bulbus.client.content.models

import net.minecraft.client.model.Model
import net.minecraft.client.model.geom.{ModelPart, PartPose}
import net.minecraft.client.model.geom.builders.{CubeListBuilder, LayerDefinition, MeshDefinition}
import net.minecraft.client.renderer.rendertype.RenderTypes

class TunableChestModel(root: ModelPart) extends Model[Unit](root, RenderTypes.entityCutoutCull)

object TunableChestModel:
  def createLayer(): LayerDefinition =
    val mesh = new MeshDefinition()
    val root = mesh.getRoot
    root.addOrReplaceChild("first", CubeListBuilder.create().texOffs(0, 0).addBox(0f, 0f, 0f, 2f, 2f, 4f), PartPose.ZERO)
    LayerDefinition.create(mesh, 16, 16)
