package io.github.quark.stage

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import akka.stream.{FlowShape, Graph}
import io.github.quark.route.{Route, RouteStatus}
import io.github.quark.stage.PipelineStage.{Input, Output}

trait PipelineStage { this: RequestProcessingStage =>

  protected def partitionFn: Input => Int

  protected def requestProcessingGraph: Graph[FlowShape[Input, Output], _]

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

  private val inputStage = Flow[Input]
  private val mergeStage = Merge[HttpResponse](2)
  private val partitionStage = Partition[Input](2, partitionFn)
  private val notFoundFlow = Flow[Input].map(_ => HttpResponse(status = 404))

}

object PipelineStage {
  type Input = HttpRequest
  type Output = HttpResponse

  def apply(routes: Route): PipelineStage =
    new PipelineStage with RequestProcessingStage {
      override protected def partitionFn: (Input) => Int =
        req =>
          routes(req) match {
            case RouteStatus.Matched(_) => 1
            case RouteStatus.UnMatched => 0
        }
    }
}
