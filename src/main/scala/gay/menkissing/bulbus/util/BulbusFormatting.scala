package gay.menkissing.bulbus.util

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants

object BulbusFormatting:
  def formatMB(amount: Long): String =
    val mb = amount.toFloat / 81f
    if mb < 1000 then
      mb.toLong.toString
    else if mb < 1_000_000 then
      String.format("%1$.2fK", mb / 1000f)
    else
      String.format("%1$.2fM", mb / 1_000_000f)
      
  def formatBuckets(amount: Long): String =
    val buckets = math.round(amount.toFloat / FluidConstants.BUCKET)
    buckets.toString
