package gay.menkissing.bulbus.client.gui

import gay.menkissing.bulbus.registries.BulbusScreens
import net.minecraft.client.gui.screens.MenuScreens

object BulbusGuis:
  def register(): Unit =
    MenuScreens.register(BulbusScreens.toolContainer, ToolContainerGui.apply)
