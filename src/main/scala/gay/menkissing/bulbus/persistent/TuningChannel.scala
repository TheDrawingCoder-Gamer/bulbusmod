package gay.menkissing.bulbus.persistent

import net.minecraft.world.item.DyeColor
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

final case class TuningChannel(first: DyeColor, second: DyeColor, third: DyeColor)

object TuningChannel:
  val CODEC: Codec[TuningChannel] =
    RecordCodecBuilder.create: inst =>
      inst.group(
        DyeColor.CODEC.fieldOf("first").forGetter(_.first),
        DyeColor.CODEC.fieldOf("second").forGetter(_.second),
        DyeColor.CODEC.fieldOf("third").forGetter(_.third)
      ).apply(inst, TuningChannel.apply)
  
  val DEFAULT: TuningChannel = TuningChannel(DyeColor.WHITE, DyeColor.WHITE, DyeColor.WHITE)