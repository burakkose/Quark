package io.github.quark.operation

import io.github.quark.route.{Route, RouteStatus}
import io.github.quark.stage.PipelineStage.Input
import shapeless.ops.hlist.IsHCons
import shapeless.{HList, LUBConstraint}

sealed trait GatewayAction

object GatewayAction {

  final case class Gateway[L <: HList](services: L)(
      implicit lUBConstraint1: LUBConstraint[L, Route],
      isHCons: IsHCons[L])
      extends GatewayAction {
    def service(request: Input)(
        implicit selector: ServiceSelector[L]): RouteStatus = {
      selector(services, request)
    }
  }

}
