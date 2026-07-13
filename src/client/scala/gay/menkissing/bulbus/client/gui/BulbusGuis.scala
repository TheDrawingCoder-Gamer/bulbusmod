package gay.menkissing.bulbus.client.gui

import gay.menkissing.bulbus.registries.BulbusScreens
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.inventory.ContainerScreen

object BulbusGuis:
  def register(): Unit =
    MenuScreens.register(BulbusScreens.toolContainer, ToolContainerGui.apply)
    MenuScreens.register(BulbusScreens.holdingBagMenu, ContainerScreen.apply)
    MenuScreens.register(BulbusScreens.tunableChestMenu, ContainerScreen.apply)
    MenuScreens.register(BulbusScreens.stasisStorageMenu, StasisStorageGui.apply)
