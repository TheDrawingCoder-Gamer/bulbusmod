package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.components.StorageItemContents
import gay.menkissing.bulbus.content.item.{StasisBottleItem, StasisTubeItem}
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.minecraft.core.Registry
import net.minecraft.core.registries.{BuiltInRegistries, Registries}
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object BulbusItems:
  def register[T <: Item](name: String, props: Item.Properties, itemFactory: Item.Properties => T): T =
    val id = BulbusMod.locate(name)
    val goodProps = props.setId(ResourceKey.create(Registries.ITEM, id))
    val goodItem = itemFactory(goodProps)
    Registry.register(BuiltInRegistries.ITEM, BulbusMod.locate(name), goodItem)
    goodItem

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
    Item.apply
  )
  
  val toolContainer: Item = register(
    "tool_container",
    Item.Properties()
        .stacksTo(1),
    Item.apply
  )

  val bulbusTab = FabricCreativeModeTab.builder()
                                       .icon(() => ItemStack(stasisBottle))
                                       .title(Component.translatable(BulbusTranslationKeys.tab))
                                       .displayItems: (params, output) =>
                                         output.accept(stasisTube)
                                         output.accept(stasisBottle)
                                         output.accept(stasisBattery)
                                         output.accept(holdingBag)
                                         output.accept(toolContainer)
                                       .build()


  def init(): Unit =
    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BulbusMod.locate("tab"), bulbusTab)

    FluidStorage.ITEM.registerForItems(
      // todo max
      (stack, c) => StasisBottleItem.StasisBottleStorage(c, StasisBottleItem.baseMax),
      stasisBottle
    )

    ItemStorage.ITEM.registerForItems(
      // todo max
      (stack, c) => StasisTubeItem.StasisTubeStorage(c, StasisTubeItem.baseMax),
      stasisTube
    )