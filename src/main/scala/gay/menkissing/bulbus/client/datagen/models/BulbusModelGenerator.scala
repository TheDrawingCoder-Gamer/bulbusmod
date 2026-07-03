package gay.menkissing.bulbus.client.datagen.models

import com.mojang.math.Transformation
import gay.menkissing.bulbus.client.content.itemmodels.{BottleFluidContentsModel, TubeStoredItemSpecialRenderer}
import gay.menkissing.bulbus.registries.BulbusItems
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.client.data.models.model.{ItemModelUtils, ModelLocationUtils, ModelTemplate, ModelTemplates, TextureMapping}
import net.minecraft.client.data.models.{BlockModelGenerators, ItemModelGenerators}
import net.minecraft.client.renderer.item.SelectItemModel.SwitchCase
import net.minecraft.client.renderer.item.properties.select.DisplayContext
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.{Item, ItemDisplayContext}
import org.joml.{Matrix4f, Vector4f}

import java.util.Optional
import java.util as ju

class BulbusModelGenerator(output: FabricPackOutput) extends FabricModelProvider(output):
  override def generateBlockStateModels(blockModelGenerators: BlockModelGenerators): Unit = ()

  // get a block material from an item because fuck you thats why
  // need this to prevent translucency shenanagins from fucking us up
  // apparently fixed in 26.2, so we can move our items back to the item atlas?
  def evilBlockTexture(item: Item): Material =
    val id = BuiltInRegistries.ITEM.getKey(item)
    Material(id.withPrefix("block/"))

  def tube(itemModelGenerators: ItemModelGenerators): Unit =
    val texMapping = TextureMapping.layer0(evilBlockTexture(BulbusItems.stasisTube))
    val baseTube = ItemModelUtils
      .plainModel(ModelTemplates.FLAT_ITEM.create(BulbusItems.stasisTube, texMapping, itemModelGenerators.modelOutput))
    val itemSubTransform = Matrix4f()
    itemSubTransform.translate(0.5f, 0.5f, 1f)
    itemSubTransform.scale(0.5f, 0.5f, 0.5f)
    itemSubTransform.translate(0.5f, 0.5f, 0.5f)
    val itemSubModel = TubeStoredItemSpecialRenderer.Unbaked(Optional.of(Transformation(itemSubTransform)))
    val tubeModel = ItemModelUtils.select(DisplayContext(), baseTube,
      SwitchCase(ju.List.of(ItemDisplayContext.GUI), ItemModelUtils.composite(baseTube, itemSubModel))
    )
    itemModelGenerators.itemModelOutput.accept(BulbusItems.stasisTube, tubeModel)

  def bottle(itemModelGenerators: ItemModelGenerators): Unit =
    val texMapping = TextureMapping.layer0(evilBlockTexture(BulbusItems.stasisBottle))
    val baseBottle = ItemModelUtils
      .plainModel(ModelTemplates.FLAT_ITEM.create(BulbusItems.stasisBottle, texMapping, itemModelGenerators.modelOutput))
    val itemSubTransform = Matrix4f()
    itemSubTransform.translate(0f, 0f, 1f)
    val itemSubModel = BottleFluidContentsModel.Unbaked(Optional.of(Transformation(itemSubTransform)), Vector4f(7f, 7f, 15f, 15f))
    val bottleModel = ItemModelUtils.select(DisplayContext(), baseBottle,
      SwitchCase(ju.List.of(ItemDisplayContext.GUI), ItemModelUtils.composite(baseBottle, itemSubModel))
    )
    itemModelGenerators.itemModelOutput.accept(BulbusItems.stasisBottle, bottleModel)
  override def generateItemModels(itemModelGenerators: ItemModelGenerators): Unit =

    bottle(itemModelGenerators)
    tube(itemModelGenerators)


    itemModelGenerators.generateFlatItem(BulbusItems.stasisBattery, ModelTemplates.FLAT_ITEM)
    itemModelGenerators.generateFlatItem(BulbusItems.toolContainer, ModelTemplates.FLAT_ITEM)
    itemModelGenerators.generateFlatItem(BulbusItems.holdingBag, ModelTemplates.FLAT_ITEM)
