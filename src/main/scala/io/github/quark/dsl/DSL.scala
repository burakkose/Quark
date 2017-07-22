package io.github.quark.dsl

import io.github.quark.action.GatewayAction.Gateway
import io.github.quark.action.OperationAction._
import io.github.quark.action.ServiceAction.Service
import io.github.quark.action.{OperationAction, ServiceAction}
import io.github.quark.route.{Route, RouteBuilder}
import io.github.quark.stage.PipelineStage.{Input, Output}
import shapeless.ops.hlist.{IsHCons, ToTraversable}
import shapeless.{HList, IsDistinctConstraint, LUBConstraint}

sealed trait DSL

trait OperationDSL extends DSL {
  def incoming(f: Input => OperationOutput[Input]): Incoming =
    Incoming(f)

  def outgoing(f: Output => OperationOutput[Output]): Outgoing =
    Outgoing(f)

  def endpoint(f: Input => OperationOutput[Output]): Endpoint =
    Endpoint(f)

}

trait ServiceDSL extends DSL {
  def service[L <: HList](path: String)(operations: L)(
      implicit lUBConstraint: LUBConstraint[L, OperationAction],
      isDistinctConstraint: IsDistinctConstraint[L],
      toTraversableAux: ToTraversable.Aux[L, List, OperationAction],
      isHCons: IsHCons[L]): Service =
    Service(path, operations.toList[OperationAction])
}

trait GatewayDSL extends DSL {
  def gateway[L1 <: HList, L2 <: HList](services: L1)(
      implicit builder: RouteBuilder.Aux[L1, L2],
      isHCons1: IsHCons[L1],
      lUBConstraint: LUBConstraint[L1, ServiceAction],
      toTraversableAux: ToTraversable.Aux[L2, List, Route]): Gateway =
    Gateway(builder(services).toList[Route])
}

object DSL extends OperationDSL with ServiceDSL with GatewayDSL
