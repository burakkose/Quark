package io.github.quark.operation

import io.github.quark.operation.OperationAction.OperationOutput
import io.github.quark.operation.OperationResult.{Abort, Success}
import io.github.quark.stage.PipelineStage.{Input, Output}

sealed trait OperationResult[+T]

object OperationResult {
  final case class Success[T](result: T) extends OperationResult[T]
  final case class Abort[T](cause: String) extends OperationResult[T]
}

trait OperationAction {
  type L
  type R

  def apply(v1: L): OperationResult[R] = f(v1) match {
    case Right(v2) => Success(v2)
    case Left(cause) => Abort(cause)
  }

  protected val f: L => OperationOutput[R]
}

object OperationAction {
  type Aux[L0, R0] = OperationAction {
    type L = L0
    type R = R0
  }

  type OperationF[A, B] = (A) => B
  type OperationOutput[A] = Either[String, A]

  final case class Incoming(f: OperationF[Input, OperationOutput[Input]])
      extends OperationAction {
    type L = Input
    type R = Input
  }

  final case class Outgoing(f: OperationF[Output, OperationOutput[Output]])
      extends OperationAction {
    type L = Output
    type R = Output
  }

  final case class Endpoint(f: OperationF[Input, OperationOutput[Output]])
      extends OperationAction {
    type L = Input
    type R = Output
  }

  implicit val withDefaultIncoming = Incoming(req => Right(req))

  implicit val withDefaultOutgoing = Outgoing(res => Right(res))

  implicit val withDefaultEndpoint = Endpoint(null)

}
