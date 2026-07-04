package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.content.item.{HoldingBagItem, StasisBottleItem, StasisTubeItem, ToolContainerItem}
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.{BlockItem, Item, ItemStack}
import net.minecraft.world.level.block.Block

object BulbusItems:
  def register[T <: Item](name: String, props: Item.Properties, itemFactory: Item.Properties => T): T =
    val id = BulbusMod.locate(name)
    val goodProps = props.setId(ResourceKey.create(Registries.ITEM, id))
    val goodItem = itemFactory(goodProps)
    Registry.register(BuiltInRegistries.ITEM, BulbusMod.locate(name), goodItem)
    goodItem


  def registerBlock(block: Block, props: Item.Properties = Item.Properties()): BlockItem =
    val blockId = block.properties().blockIdOrThrow()
    val itemId = ResourceKey.create(Registries.ITEM, blockId.identifier())
    val item = new BlockItem(block, props.setId(itemId).useBlockDescriptionPrefix())
    item.registerBlocks(Item.BY_BLOCK, item)

    Registry.register(BuiltInRegistries.ITEM, itemId, item)

  val stasisBottle: Item = register(
    "stasis_bottle",
    Item.Properties()
        .enchantable(5)
        .component(BulbusDataComponentTypes.STASIS_BOTTLE_CONTENTS, StorageItemContents.Fluid.DEFAULT)
        .stacksTo(1),
    StasisBottleItem.apply
  )

  val stasisTube: Item = register(
    "stasis_tube",
    Item.Properties()
        .enchantable(5)
        .component(BulbusDataComponentTypes.STASIS_TUBE_CONTENTS, StorageItemContents.Item.DEFAULT)
        .stacksTo(1),
    StasisTubeItem.apply
  )

  val stasisBattery: Item = register(
    "stasis_battery",
    Item.Properties()
        .enchantable(5)
        .stacksTo(1),
    Item.apply
  )

  val holdingBag: Item = register(
    "holding_bag",
    Item.Properties()
        .stacksTo(1),
    HoldingBagItem.apply
  )
  
  val toolContainer: Item = register(
    "tool_container",
    Item.Properties()
        .stacksTo(1),
    ToolContainerItem.apply
  )

  val stasisShelf: Item = registerBlock(BulbusBlocks.stasisShelf)

  val bulbusTab = FabricCreativeModeTab.builder()
                                       .icon(() => ItemStack(stasisBottle))
                                       .title(Component.translatable(BulbusTranslationKeys.tab))
                                       .displayItems: (params, output) =>
                                         output.accept(stasisTube)
                                         output.accept(stasisBottle)
                                         output.accept(stasisBattery)
                                         output.accept(holdingBag)
                                         output.accept(toolContainer)
                                         output.accept(stasisShelf)
                                       .build()


  def init(): Unit =
    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BulbusMod.locate("tab"), bulbusTab)

    FluidStorage.ITEM.registerForItems(
      (stack, c) => StasisBottleItem.StasisBottleStorage(c, StasisBottleItem.getMaxEvil(stack)),
      stasisBottle
    )

    ItemStorage.ITEM.registerForItems(
      (stack, c) => StasisTubeItem.StasisTubeStorage(c, StasisTubeItem.getMaxEvil(stack)),
      stasisTube
    )