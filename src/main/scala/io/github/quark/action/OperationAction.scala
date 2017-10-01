package io.github.quark.action

import io.github.quark.action.OperationAction.OperationOutput
import io.github.quark.action.OperationResult.{Abort, Success}
import io.github.quark.stage.PipelineStage.{Input, Output}

import scala.concurrent.{ExecutionContext, Future}

sealed trait OperationResult[+T]

object OperationResult {
  final case class Success[T](result: T) extends OperationResult[T]
  final case class Abort[T](cause: String) extends OperationResult[T]
}

trait OperationAction {
  type L
  type R

  def apply(v1: L)(implicit ec: ExecutionContext): Future[OperationResult[R]] =
    f(v1).map {
      case Right(v2) => Success(v2)
      case Left(cause) => Abort(cause)
    }

  protected val f: L => OperationOutput[R]
}

object OperationAction {
  type OperationOutput[A] = Future[Either[String, A]]

  final case class Incoming(f: Input => OperationOutput[Input])
      extends OperationAction {
    type L = Input
    type R = Input
  }

  final case class Endpoint(f: Input => OperationOutput[Output])
      extends OperationAction {
    type L = Input
    type R = Output
  }

  final case class Outgoing(f: Output => OperationOutput[Output])
      extends OperationAction {
    type L = Output
    type R = Output
  }
}
