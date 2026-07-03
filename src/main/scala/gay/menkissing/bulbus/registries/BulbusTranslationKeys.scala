package gay.menkissing.bulbus.registries

import net.minecraft.network.chat.Component
import gay.menkissing.bulbus.util.BulbusFormatting

object BulbusTranslationKeys:
  val tab = "creative_tab.bulbus"
  
  object stasisBottle:
    object tooltip:
      val usagePickup: String = BulbusItems.stasisBottle.getDescriptionId + ".tooltip.usage_pickup"
      val usagePlace: String = BulbusItems.stasisBottle.getDescriptionId + ".tooltip.usage_place"
      
      val empty: String = BulbusItems.stasisBottle.getDescriptionId + ".tooltip.empty"
      
      val countMB: String = BulbusItems.stasisBottle.getDescriptionId + ".tooltip.count_mb"
      
      def showCountMB(amount: Long, max: Long): Component =
        Component.translatable(countMB, BulbusFormatting.formatMB(amount), BulbusFormatting.formatBuckets(max))

  object stasisTube:
    object tooltip:
      val empty: String = BulbusItems.stasisTube.getDescriptionId + ".tooltip.empty"
      val count: String = BulbusItems.stasisTube.getDescriptionId + ".tooltip.count"

      def showCount(amount: Long, max: Long, stacksSize: String): Component =
        Component.translatable(count, amount, max, stacksSize)