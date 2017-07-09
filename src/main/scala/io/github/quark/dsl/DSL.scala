package io.github.quark.dsl

import io.github.quark.route.{Route, RouteBuilder}
import io.github.quark.operation.GatewayAction.Gateway
import io.github.quark.operation.OperationAction._
import io.github.quark.operation.ServiceAction.Service
import io.github.quark.operation.{OperationAction, ServiceAction}
import io.github.quark.stage.PipelineStage.{Input, Output}
import shapeless.ops.hlist.IsHCons
import shapeless.{HList, IsDistinctConstraint, LUBConstraint}

sealed trait DSL

trait OperationDSL extends DSL {
  def incoming(f: OperationF[Input, OperationOutput[Input]]): Incoming =
    Incoming(f)

  def outgoing(f: OperationF[Output, OperationOutput[Output]]): Outgoing =
    Outgoing(f)

  def endpoint(f: OperationF[Input, OperationOutput[Output]]): Endpoint =
    Endpoint(f)

}

trait ServiceDSL extends DSL {
  def service[L <: HList](path: String)(operations: L)(
      implicit lUBConstraint: LUBConstraint[L, OperationAction],
      isDistinctConstraint: IsDistinctConstraint[L],
      isHCons: IsHCons[L]): Service[L] =
    Service(path)(operations)
}

trait GatewayDSL extends DSL {
  def gateway[L1 <: HList, L2 <: HList](services: L1)(
      implicit builder: RouteBuilder.Aux[L1, L2],
      isHCons1: IsHCons[L1],
      isHCons2: IsHCons[L2],
      lUBConstraint1: LUBConstraint[L1, ServiceAction],
      lUBConstraint2: LUBConstraint[L2, Route]): Gateway[L2] =
    Gateway(builder(services))
}

object DSL extends OperationDSL with ServiceDSL with GatewayDSL
