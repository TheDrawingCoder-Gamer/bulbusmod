package gay.menkissing.bulbus.client.datagen.models

import com.mojang.math.{Quadrant, Transformation}
import gay.menkissing.bulbus.client.content.itemmodels.{BottleFluidContentsModel, TubeStoredItemSpecialRenderer}
import gay.menkissing.bulbus.client.datagen.models.BulbusModelGenerator.*
import gay.menkissing.bulbus.registries.{BulbusBlocks, BulbusItems}
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.client.data.models.blockstates.{MultiVariantGenerator, PropertyDispatch}
import net.minecraft.client.data.models.model.*
import net.minecraft.client.data.models.{BlockModelGenerators, ItemModelGenerators, MultiVariant}
import net.minecraft.client.renderer.block.dispatch.{Variant, VariantMutator}
import net.minecraft.client.renderer.item.SelectItemModel.SwitchCase
import net.minecraft.client.renderer.item.properties.select.DisplayContext
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.util.random.WeightedList
import net.minecraft.world.item.{Item, ItemDisplayContext}
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.joml.{Matrix4f, Vector4f}

import java.util as ju
import java.util.Optional

class BulbusModelGenerator(output: FabricPackOutput) extends FabricModelProvider(output):
  def barrelLikeModels(block: Block, blockModelGenerators: BlockModelGenerators): (Identifier, Identifier) =
    val closedModel = TexturedModel.CUBE_TOP_BOTTOM.create(block, blockModelGenerators.modelOutput)
    val openMapping = (new TextureMapping)
      .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
      .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_open"))
      .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"))
    val openModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(block, "_open", openMapping, blockModelGenerators.modelOutput)
    (closedModel, openModel)

  override def generateBlockStateModels(blockModelGenerators: BlockModelGenerators): Unit =
    val customAccessorModel = ModelLocationUtils.getModelLocation(BulbusBlocks.stasisAccessor)
    blockModelGenerators.blockStateOutput.accept:
      BlockModelGenerators.createSimpleBlock(BulbusBlocks.stasisAccessor, BlockModelGenerators.plainVariant(customAccessorModel))
    blockModelGenerators.registerSimpleItemModel(BulbusBlocks.stasisAccessor, customAccessorModel)

    val (closedShelf, openShelf) = barrelLikeModels(BulbusBlocks.stasisShelf, blockModelGenerators)
    // somehow made this more complex between versions, thanks mojang
    blockModelGenerators.blockStateOutput.accept:
      MultiVariantGenerator.dispatch(BulbusBlocks.stasisShelf).`with`:
        PropertyDispatch.initial(BlockStateProperties.OPEN)
                        .selectV(false, Variant(closedShelf))
                        .selectV(true, Variant(openShelf))
  
    val (closedWorm, openWorm) = barrelLikeModels(BulbusBlocks.stasisWorm, blockModelGenerators)
    blockModelGenerators.blockStateOutput.accept:
      MultiVariantGenerator.dispatch(BulbusBlocks.stasisWorm).`with`:
        PropertyDispatch.initial(BlockStateProperties.OPEN)
                        .selectV(false, Variant(closedWorm))
                        // i tremor at the pour soul who looks into an open worm
                        .selectV(true, Variant(openWorm))
      .`with`:
        PropertyDispatch.modify(BlockStateProperties.FACING).generate: dir =>
          val xRot =
            dir match
              case Direction.DOWN => Quadrant.R180
              case Direction.UP => Quadrant.R0
              case _ => Quadrant.R90
          val yRot =
            dir match
              case Direction.SOUTH => Quadrant.R180
              case Direction.WEST => Quadrant.R270
              case Direction.EAST => Quadrant.R90
              case _ => Quadrant.R0
          
          VariantMutator.X_ROT.withValue(xRot).`then`(VariantMutator.Y_ROT.withValue(yRot))

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
    itemModelGenerators.generateFlatItem(BulbusItems.knowledgeStorage, ModelTemplates.FLAT_ITEM)


object BulbusModelGenerator:
  extension[T <: Comparable[T]] (self: PropertyDispatch.C1[MultiVariant, T])
    def selectV(value: T, variant: Variant): self.type =
      self.select(value, MultiVariant(WeightedList.of(variant)))
      self