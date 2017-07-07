package io.github.quark.dsl

import io.github.quark.operation.OperationAction
import io.github.quark.operation.OperationAction._
import io.github.quark.operation.ServiceAction.Service
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

trait ServiceDSL {
  def service[L <: HList](path: String)(operations: L)(
      implicit lUBConstraint: LUBConstraint[L, OperationAction],
      isDistinctConstraint: IsDistinctConstraint[L],
      isHCons: IsHCons[L]): Service[L] =
    Service(path)(operations)
}

object DSL extends OperationDSL with ServiceDSL
