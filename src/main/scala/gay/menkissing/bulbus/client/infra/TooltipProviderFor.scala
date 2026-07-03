package gay.menkissing.bulbus.client.infra

import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.core.component.{DataComponentGetter, DataComponentType}
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.TooltipFlag

@Environment(EnvType.CLIENT)
trait TooltipProviderFor[T]:
  def addToTooltip(self: T, tooltip: TooltipContext, textConsumer: Component => Unit, kind: TooltipFlag, components: DataComponentGetter): Unit

@Environment(EnvType.CLIENT)
object TooltipProviderFor:
  def register[T](componentKind: DataComponentType[T])(provider: TooltipProviderFor[T]): Unit =
    ItemTooltipCallback.EVENT.register((stack, context, kind, tooltip) =>
      val component = stack.get(componentKind)
      if component != null then
        provider.addToTooltip(component, context, it => tooltip.add(it), kind, stack)
    )