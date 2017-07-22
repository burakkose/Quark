package io.github.quark.stage

import akka.stream._
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}
import io.github.quark.action.OperationAction
import io.github.quark.action.OperationResult.{Abort, Success}
import io.github.quark.route.RouteStatus
import io.github.quark.stage.StageResult._

trait OperationStage[In, Out] {

  protected def stageName: String

  protected def defaultOperation: Operation

  private type Operation = OperationAction {
    type L = In
    type R = Out
  }

  def buildGraph = {
    GraphDSL
      .create() { implicit builder =>
        val partition = builder.add(partitionFlow)
        val processing = builder.add(processingFlow)

        // format: off
        processing ~> partition.in
        // format: on

        UniformFanOutShape(processing.in, partition.out(0), partition.out(1))
      }
      .named(stageName)
  }

  private val partitionFlow =
    Partition[(StageResult[Out], RouteStatus)](
      2,
      tup => if (tup._1.isSuccess) 1 else 0)

  private val processingFlow =
    Flow[(In, RouteStatus)].map {
      case (input, routeStatus) =>
        val result = routeStatus match {
          case RouteStatus.Matched(service) =>
            val operation: Operation =
              service.operation[Operation].getOrElse(defaultOperation)
            operation.apply(input) match {
              case Success(res) => Complete(res)
              case Abort(cause) => Failed(StageFailCause(cause))
            }
          case RouteStatus.UnMatched =>
            Failed(StageFailCause("not service found"))
        }
        (result, routeStatus)
    }
}

object OperationStage {
  def fromOutletResult[T](outlet: Outlet[(StageResult[T], RouteStatus)])(
      implicit b: Builder[_]): PortOps[(T, RouteStatus)] =
    port2flow(outlet).map { tup =>
      val unwrapped = tup._1 match {
        case Complete(res) => res
        case _ => throw new MatchError("Output flow excepts completed result.")
      }
      (unwrapped, tup._2)
    }
}
