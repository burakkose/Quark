package io.github.quark.stage

import akka.http.scaladsl.model.HttpResponse
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}
import io.github.quark.stage.OperationStage.OperationGraph
import io.github.quark.stage.PipelineStage.{Input, Output}
import io.github.quark.stage.StageResult._

trait OperationStage[In, Out] {

  def stageName: String

  def flow: Flow[In, StageResult[Out], _]

  def buildStageGraph(): OperationGraph[In, Out] = {
    GraphDSL
      .create() { implicit builder =>
        val input = builder.add(in)
        val partition = builder.add(partitionFlow)
        val processing = builder.add(flow)

        // format: off
        input ~> processing ~> partition.in
        // format: on

        UniformFanOutShape(input.in, partition.out(0), partition.out(1))
      }
      .named(stageName)
  }

  private val partitionFlow =
    Partition[StageResult[Out]](2, res => if (res.isSuccess) 1 else 0)

  private val in = Flow[In]
}

object OperationStage {

  type OperationGraph[In, Out] =
    Graph[UniformFanOutShape[In, StageResult[Out]], _]

  def apply[In, Out](implicit operationStage: OperationStage[In, Out])
    : OperationGraph[In, Out] = operationStage.buildStageGraph()

  implicit object IncomingOperationStage extends OperationStage[Input, Input] {

    override val flow: Flow[Input, StageResult[Input], _] =
      Flow[Input].map(req => Complete(req))

    override val stageName: String = "incoming-flow"
  }

  implicit object EndpointOperationStage
      extends OperationStage[Input, Output] {

    override val flow: Flow[Input, StageResult[Output], _] =
      Flow[Input].map(_ => Complete(HttpResponse(entity = "kekekegt")))

    override val stageName: String = "endpoint-flow"
  }

  implicit object OutgoingOperationStage
      extends OperationStage[Output, Output] {

    override val flow: Flow[Output, StageResult[Output], _] =
      Flow[Output].map(Complete.apply)

    override val stageName: String = "outgoing-flow"
  }

  implicit def fromOutletResult[T](outlet: Outlet[StageResult[T]])(
      implicit b: Builder[_]): PortOps[T] = port2flow(outlet).map {
    case Complete(result) => result
    case _ => throw new MatchError("Output flow excepts completed result.")
  }
}
