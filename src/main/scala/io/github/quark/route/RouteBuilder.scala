package io.github.quark.route

import io.github.quark.operation.ServiceAction
import shapeless.{::, DepFn1, HList, HNil}

trait RouteBuilder[L <: HList] extends DepFn1[L] {
  type Out <: HList
}

object RouteBuilder {

  def apply[L <: HList](
      implicit builder: RouteBuilder[L]): Aux[L, builder.Out] = builder

  type Aux[L <: HList, Out0 <: HList] = RouteBuilder[L] { type Out = Out0 }

  implicit def build[L <: HList, Out0 <: HList](
      implicit build: RouteBuilder0[HNil, L, Out0]): Aux[L, Out0] =
    new RouteBuilder[L] {
      type Out = Out0

      def apply(l: L): Out = build(HNil, l)
    }

  trait RouteBuilder0[Acc <: HList, L <: HList, Out <: HList] {
    def apply(acc: Acc, l: L): Out
  }

  object RouteBuilder0 {
    implicit def hNilBuilder[Out <: HList]: RouteBuilder0[Out, HNil, Out] =
      new RouteBuilder0[Out, HNil, Out] {
        def apply(acc: Out, l: HNil): Out = acc
      }

    implicit def hListBuilder[Acc <: HList,
                              H <: ServiceAction,
                              T <: HList,
                              Out <: HList](
        implicit builder: RouteBuilder0[Route :: Acc, T, Out])
      : RouteBuilder0[Acc, H :: T, Out] =
      new RouteBuilder0[Acc, H :: T, Out] {
        def apply(acc: Acc, l: H :: T): Out =
          builder(Route.instance(l.head) :: acc, l.tail)
      }
  }

}
