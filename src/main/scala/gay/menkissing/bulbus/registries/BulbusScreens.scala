package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.screen.{HoldingBagMenu, StasisStorageMenu, ToolContainerMenu}
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.flag.{FeatureFlagSet, FeatureFlags}
import net.minecraft.world.inventory.{ChestMenu, MenuType}
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import scala.annotation.nowarn

object BulbusScreens:
  val toolContainer: ExtendedMenuType[ToolContainerMenu, Boolean] = 
    new ExtendedMenuType(ToolContainerMenu.fromNetwork, ByteBufCodecs.BOOL.map(_.booleanValue(), Boolean.box))
  
  def sup[T <: AbstractContainerMenu](f: (Int, Inventory) => T): MenuType.MenuSupplier[T] =
    // ???????????
    new MenuType.MenuSupplier[T]:
      def create(containerId: Int, inventory: Inventory): T = f(containerId, inventory)

  def simpleMenu[T <: AbstractContainerMenu](f: (Int, Inventory) => T): MenuType[T] =
    MenuType(f.apply: @nowarn, FeatureFlags.VANILLA_SET)
    

  val tunableChestMenu: MenuType[ChestMenu] =
    simpleMenu(ChestMenu.threeRows)

  val holdingBagMenu: MenuType[ChestMenu] =
    simpleMenu(HoldingBagMenu.fromNetwork)
  
  val stasisStorageMenu: MenuType[StasisStorageMenu] =
    simpleMenu(StasisStorageMenu.client)
  
  def init(): Unit =
    Registry.register(BuiltInRegistries.MENU, BulbusMod.locate("tool_container"), toolContainer)
    Registry.register(BuiltInRegistries.MENU, BulbusMod.locate("holding_bag"), holdingBagMenu)
    Registry.register(BuiltInRegistries.MENU, BulbusMod.locate("stasis_storage"), stasisStorageMenu)