package io.github.quark.flow

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import akka.stream.{FlowShape, Graph}
import io.github.quark.flow.PipelineFlow.Implicits._
import io.github.quark.flow.PipelineFlow.Input
import io.github.quark.route.QuarkRouteStatus.{FAILED, SUCCESS}
import io.github.quark.route.{QuarkRoute, QuarkRouteStatus}

trait PipelineFlow extends RequestProcessingFlow {

  protected def partitionFn: Input => Int

  def pipelineFlow: Graph[FlowShape[Input, HttpResponse], _] =
    GraphDSL
      .create() { implicit builder =>
        import GraphDSL.Implicits._

        val input = builder.add(inputStage)
        val partition = builder.add(partitionStage)
        val merge = builder.add(mergeStage)
        val notFoundResponse = builder.add(notFoundFlow)
        val foundResponse = builder.add(foundFlow)

        // format: off
        input ~> partition.in
                 partition.out(SUCCESS) ~> foundResponse    ~> merge.in(SUCCESS)
                 partition.out(FAILED)  ~> notFoundResponse ~> merge.in(FAILED)
        // format: on

        FlowShape(input.in, merge.out)
      }
      .named("pipeline-flow")

  private val inputStage = Flow[Input]
  private val partitionStage = Partition[Input](2, partitionFn)
  private val mergeStage = Merge[HttpResponse](2)
  private val notFoundFlow = Flow[Input].map(_ => HttpResponse(status = 404))

}

object PipelineFlow {
  type Input = HttpRequest
  type Output = HttpResponse

  def apply(routes: QuarkRoute): PipelineFlow =
    new PipelineFlow {
      override protected def partitionFn: (Input) => Int = req => routes(req)
    }

  object Implicits {
    implicit def quarkRouteStatusToOutput(status: QuarkRouteStatus): Int =
      status match {
        case QuarkRouteStatus.SUCCESS => 1
        case QuarkRouteStatus.FAILED => 0
      }
  }

}
