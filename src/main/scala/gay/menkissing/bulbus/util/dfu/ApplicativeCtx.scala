package gay.menkissing.bulbus.util.dfu

import com.mojang.datafixers.kinds.*
import java.util as ju
import ju.function.Function

opaque type ApplicativeCtx[F <: K1, Src <: App[F, _]] =  Applicative[F, ?]

trait ApplicativeCtxGivens:
  given catsAppGivenAppCtx[F <: App[?, _]](using hasDfu: HasDFUApplicative[F]): cats.Applicative[F] with
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] = 
      val instance = hasDfu.summonInstance
      val goodFF = instance.map[A => B, Function[A, B]](it => (a) => it(a), ff.asInstanceOf[hasDfu.RealSrc[A => B]])
      instance.ap(goodFF, fa.asInstanceOf[hasDfu.RealSrc[A]]).asInstanceOf[F[B]]
    def pure[A](x: A): F[A] =
      val instance = hasDfu.summonInstance
      instance.point(x).asInstanceOf[F[A]]

object ApplicativeCtx extends ApplicativeCtxGivens:
  // F is Src.Mu
  // Mu is the instances Mu
  // Src is the original type
  def apply[F <: K1](it: Applicative[F, ?])[Src <: App[F, _]](): ApplicativeCtx[F, Src] = it



  