package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.screen.{HoldingBagMenu, StasisStorageMenu, ToolContainerMenu}
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.flag.{FeatureFlagSet, FeatureFlags}
import net.minecraft.world.inventory.{ChestMenu, MenuType}

object BulbusScreens:
  val toolContainer: ExtendedMenuType[ToolContainerMenu, Boolean] = 
    new ExtendedMenuType(ToolContainerMenu.fromNetwork, ByteBufCodecs.BOOL.map(_.booleanValue(), Boolean.box))
  
  val tunableChestMenu: MenuType[ChestMenu] =
    MenuType(ChestMenu.threeRows, FeatureFlags.VANILLA_SET)

  val holdingBagMenu: MenuType[ChestMenu] =
    MenuType(HoldingBagMenu.fromNetwork, FeatureFlags.VANILLA_SET)
  
  val stasisStorageMenu: MenuType[StasisStorageMenu] =
    MenuType(StasisStorageMenu.client, FeatureFlags.VANILLA_SET)
  
  def init(): Unit =
    Registry.register(BuiltInRegistries.MENU, BulbusMod.locate("tool_container"), toolContainer)
    Registry.register(BuiltInRegistries.MENU, BulbusMod.locate("holding_bag"), holdingBagMenu)
    Registry.register(BuiltInRegistries.MENU, BulbusMod.locate("stasis_storage"), stasisStorageMenu)