package io.github.quark.operation

import io.github.quark.route.RouteStatus.{Matched, UnMatched}
import io.github.quark.route.{Route, RouteStatus}
import io.github.quark.stage.PipelineStage.Input
import shapeless.{::, DepFn2, HList, HNil}

trait ServiceSelector[L <: HList] extends DepFn2[L, Input] {
  type Out = RouteStatus
}

object ServiceSelector {
  implicit def hNilSelector =
    new ServiceSelector[HNil] {
      def apply(t: HNil, input: Input): RouteStatus = UnMatched
    }

  implicit def hListSelector[H <: Route, T <: HList](
      implicit ss: ServiceSelector[T]) =
    new ServiceSelector[H :: T] {
      def apply(hList: ::[H, T], input: Input): RouteStatus = {
        hList.head(input) match {
          case m: Matched => m
          case UnMatched => ss(hList.tail, input)
        }
      }
    }
}
