package gay.menkissing.bulbus.util

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants

object BulbusFormatting:
  def formatMB(amount: Long): String =
    val mb = amount.toDouble / 81
    formatMagnitude(mb)
  def formatMagnitude(amount: Double): String =
    if amount < 1000 then
      amount.toLong.toString
    else if amount < 1_000_000 then
      String.format("%1$.2fK", amount / 1000f)
    else
      String.format("%1$.2fM", amount / 1_000_000f)

  def formatBuckets(amount: Long): String =
    val buckets = math.round(amount.toFloat / FluidConstants.BUCKET)
    buckets.toString
