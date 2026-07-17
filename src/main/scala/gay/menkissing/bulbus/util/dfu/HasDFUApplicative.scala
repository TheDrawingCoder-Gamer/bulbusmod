package gay.menkissing.bulbus.util.dfu

import com.mojang.datafixers.kinds.*
import com.mojang.serialization.codecs.RecordCodecBuilder

trait HasDFUApplicative[Src <: App[? <: K1, _]]:
  type Mu <: K1
  type AppInstance <: Applicative[Mu, ?]
  type RealSrc[T] <: App[Mu, T]

  type Ctx = ApplicativeCtx[Mu, RealSrc]

  def summonInstance: AppInstance

  def inContext[T](f: Ctx ?=> T): T = f(using ApplicativeCtx(summonInstance)())

trait HasDFUApplicativeGivens:
  given hasDfuForRecordCodecBuilder[O]: HasDFUApplicative[RecordCodecBuilder[O, _]] with
    type Mu = RecordCodecBuilder.Mu[O]
    type AppInstance = RecordCodecBuilder.Instance[O]
    type RealSrc = RecordCodecBuilder[O, _]

    def summonInstance: AppInstance = RecordCodecBuilder.instance[O]()

object HasDFUApplicative extends HasDFUApplicativeGivens