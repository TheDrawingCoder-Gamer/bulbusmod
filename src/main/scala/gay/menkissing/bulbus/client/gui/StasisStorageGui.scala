package gay.menkissing.bulbus.client.gui

import gay.menkissing.bulbus.screen.StasisStorageMenu
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class StasisStorageGui(menu: StasisStorageMenu, inventory: Inventory, component: Component)
  extends AbstractContainerScreen[StasisStorageMenu](menu, inventory, component):
  override def extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float): Unit =
    val i = (this.width - this.imageWidth) / 2
    val j = (this.height - this.imageHeight) / 2
    graphics.blit(RenderPipelines.GUI_TEXTURED, StasisStorageGui.texture, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256)

object StasisStorageGui:
  val texture = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png")
