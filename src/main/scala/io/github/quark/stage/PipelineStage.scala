package io.github.quark.stage

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import akka.stream.{FlowShape, Graph}
import io.github.quark.action.GatewayAction
import io.github.quark.action.OperationAction.{Endpoint, Incoming, Outgoing}
import io.github.quark.route.RouteStatus
import io.github.quark.stage.PipelineStage.{Input, Output}

trait PipelineStage { this: RequestProcessingStage =>

  protected def partitionFn: ((Input, RouteStatus)) => Int

  protected def findServiceFn: Input => RouteStatus

  protected def requestProcessingGraph
    : Graph[FlowShape[(Input, RouteStatus), Output], _]

  def pipelineFlow: Flow[Input, Output, _] =
    Flow
      .fromGraph(
        GraphDSL
          .create() { implicit builder =>
            import GraphDSL.Implicits._

            val input = builder.add(inputStage)
            val merge = builder.add(mergeStage)
            val partition = builder.add(partitionStage)
            val notFoundResponse = builder.add(notFoundFlow)
            val foundResponse = builder.add(requestProcessingGraph)

            // format: off
            input ~> partition.in
                     partition.out(1) ~> foundResponse    ~> merge.in(1)
                     partition.out(0) ~> notFoundResponse ~> merge.in(0)
            // format: on

            FlowShape(input.in, merge.out)
          }
      )
      .named("pipeline-flow")

  private val inputStage = Flow[Input].map(req => (req, findServiceFn(req)))
  private val mergeStage = Merge[HttpResponse](2)
  private val partitionStage = Partition[(Input, RouteStatus)](2, partitionFn)
  private val notFoundFlow = Flow[Any].map(_ => HttpResponse(status = 404))

}

object PipelineStage {
  type Input = HttpRequest
  type Output = HttpResponse

  def apply(gateway: GatewayAction): PipelineStage =
    new PipelineStage with RequestProcessingStage {

      protected def partitionFn: ((Input, RouteStatus)) => Int =
        tup =>
          tup._2 match {
            case RouteStatus.Matched(_) => 1
            case RouteStatus.UnMatched => 0
        }

      protected def findServiceFn: (Input) => RouteStatus =
        req => gateway.service(req)

      protected val incomingStage: OperationStage[Input, Input] =
        new OperationStage[Input, Input] {
          protected val stageName: String = "incoming-flow"

          protected val defaultOperation: Incoming = Incoming(Right.apply)
        }

      protected val outgoingStage: OperationStage[Output, Output] =
        new OperationStage[Output, Output] {
          protected val stageName: String = "outgoing-flow"

          protected val defaultOperation: Outgoing = Outgoing(Right.apply)
        }

      protected val endpointStage: OperationStage[Input, Output] =
        new OperationStage[Input, Output] {
          protected val stageName: String = "endpoint-flow"

          protected val defaultOperation: Endpoint = Endpoint(null)
        }
    }
}
