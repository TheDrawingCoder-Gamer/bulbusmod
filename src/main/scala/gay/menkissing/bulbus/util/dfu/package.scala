package gay.menkissing.bulbus.util

import cats.Applicative
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.serialization.Codec

package object dfu:
  def simpleBuiltCodec[T](f: Applicative[RecordCodecBuilder[T, _]] => RecordCodecBuilder[T, T])(using app: Applicative[RecordCodecBuilder[T, _]]): Codec[T] =
    RecordCodecBuilder.build:
      f(app)
    .codec()
