package gay.menkissing.bulbus.client.gui

import gay.menkissing.bulbus.registries.BulbusScreens
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.inventory.ContainerScreen

import annotation.nowarn

object BulbusGuis:
  def register(): Unit =
    MenuScreens.register(BulbusScreens.toolContainer, ToolContainerGui.apply: @nowarn)
    MenuScreens.register(BulbusScreens.holdingBagMenu, ContainerScreen.apply: @nowarn)
    MenuScreens.register(BulbusScreens.tunableChestMenu, ContainerScreen.apply: @nowarn)
    MenuScreens.register(BulbusScreens.stasisStorageMenu, StasisStorageGui.apply: @nowarn)
