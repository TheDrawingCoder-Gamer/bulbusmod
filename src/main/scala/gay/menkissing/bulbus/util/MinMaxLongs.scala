package gay.menkissing.bulbus.util

import net.minecraft.advancements.criterion.MinMaxBounds
import com.mojang.serialization.Codec
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import io.netty.buffer.ByteBuf

final case class MinMaxLongs(bounds: MinMaxBounds.Bounds[java.lang.Long]) extends MinMaxBounds[java.lang.Long]:
  def matches(value: Long): Boolean =
    if this.bounds.min().isPresent() && this.bounds.min().get() > value then
      false
    else
      this.bounds.max().isEmpty() || this.bounds.max().get() >= value

object MinMaxLongs:
  val ANY: MinMaxLongs = new MinMaxLongs(MinMaxBounds.Bounds.any[java.lang.Long]())


  val CODEC: Codec[MinMaxLongs] = 
    MinMaxBounds.Bounds.createCodec(Codec.LONG)
    .xmap(apply, _.bounds)
  val STREAM_CODEC: StreamCodec[ByteBuf, MinMaxLongs] = 
    MinMaxBounds.Bounds.createStreamCodec(ByteBufCodecs.LONG)
    .map(apply, _.bounds)