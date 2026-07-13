package gay.menkissing.bulbus.registries

import net.minecraft.network.chat.Component
import gay.menkissing.bulbus.util.BulbusFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
object BulbusTranslationKeys:
  val tab = "creative_tab.bulbus"

  def genericKey(keyNs: String, id: Identifier): String =
    s"$keyNs.${id.getNamespace}.${id.getPath.replace("/", ".")}"

  object container:
    def keyFor(id: Identifier): String =
      genericKey("container", id)

    def keyFor(item: Item): String =
      val id = BuiltInRegistries.ITEM.getKey(item)
      keyFor(id)

    val shelf: String = keyFor(BulbusItems.stasisShelf)
    val worm: String = keyFor(BulbusItems.stasisWorm)
    val tunableChest: String = keyFor(BulbusItems.tunableChest)

  def tooltipFor(item: Item, tooltip: String): String =
    item.getDescriptionId + ".tooltip." + tooltip

  object stasisBottle:
    object tooltip:
      val usagePickup: String = tooltipFor(BulbusItems.stasisBottle, "usage_pickup")
      val usagePlace: String = tooltipFor(BulbusItems.stasisBottle, "usage_place")
      
      val empty: String = tooltipFor(BulbusItems.stasisBottle, "empty")
      
      val countMB: String = tooltipFor(BulbusItems.stasisBottle, "count_mb")
      
      def showCountMB(amount: Long, max: Long): Component =
        Component.translatable(countMB, BulbusFormatting.formatMB(amount), BulbusFormatting.formatBuckets(max))

  object stasisTube:
    object tooltip:
      val empty: String = tooltipFor(BulbusItems.stasisTube, "empty")
      val count: String = tooltipFor(BulbusItems.stasisTube, "count")

      def showCount(amount: Long, max: Long, stacksSize: String): Component =
        Component.translatable(count, amount, max, stacksSize)

  object stasisBattery:
    object tooltip:
      val count: String = tooltipFor(BulbusItems.stasisBattery, "count")
      val transferRate: String = tooltipFor(BulbusItems.stasisBattery, "transfer_rate")

      def showCount(amount: Long, max: Long): Component =
        Component.translatable(count, BulbusFormatting.formatMagnitude(amount.toDouble), BulbusFormatting.formatMagnitude(max.toDouble))

      def showTransferRate(amount: Long): Component =
        Component.translatable(transferRate, BulbusFormatting.formatMagnitude(amount.toDouble))

  object knowledgeStorage:
    object tooltip:
      val count: String = tooltipFor(BulbusItems.knowledgeStorage, "count")
      
      def showCount(amount: Long, max: Long): Component =
        Component.translatable(count, amount.toString, max.toString)