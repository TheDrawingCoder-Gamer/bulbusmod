package gay.menkissing.bulbus.registries

import gay.menkissing.bulbus.BulbusMod
import gay.menkissing.bulbus.screen.ToolContainerMenu
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs

object BulbusScreens:
  val toolContainer: ExtendedMenuType[ToolContainerMenu, Boolean] = 
    new ExtendedMenuType(ToolContainerMenu.fromNetwork, ByteBufCodecs.BOOL.map(_.booleanValue(), Boolean.box))
  
  
  
  def init(): Unit =
    Registry.register(BuiltInRegistries.MENU, BulbusMod.locate("tool_container"), toolContainer)
