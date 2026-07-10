package gay.menkissing.bulbus.client.gui

import gay.menkissing.bulbus.screen.ToolContainerMenu
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class ToolContainerGui(menu: ToolContainerMenu, inventory: Inventory, component: Component)
  extends AbstractContainerScreen[ToolContainerMenu](menu, inventory, component, 176, 114 + ToolContainerMenu.rows * 18):
  this.inventoryLabelY = this.imageHeight - 94

  override def extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float): Unit =
    super.extractBackground(graphics, mouseX, mouseY, a)
    val xo: Int = (this.width - this.imageWidth) / 2
    val yo: Int = (this.height - this.imageHeight) / 2
    graphics.blit(RenderPipelines.GUI_TEXTURED, ToolContainerGui.texture, xo, yo, 0.0f, 0.0f, this.imageWidth, ToolContainerMenu.rows * 18 + 17, 256, 256)
    graphics.blit(RenderPipelines.GUI_TEXTURED, ToolContainerGui.texture, xo, yo + ToolContainerMenu.rows * 18 + 17, 0f, 126f, this.imageWidth, 96, 256, 256)
    
object ToolContainerGui:
  val texture: Identifier = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png")
