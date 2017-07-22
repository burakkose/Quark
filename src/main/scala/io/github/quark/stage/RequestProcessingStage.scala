package io.github.quark.stage

import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.{Flow, GraphDSL, Merge}
import akka.stream.{FlowShape, Graph}
import io.github.quark.route.RouteStatus
import io.github.quark.stage.PipelineStage.{Input, Output}
import io.github.quark.stage.StageResult.Failed

trait RequestProcessingStage {

  protected def incomingStage: OperationStage[Input, Input]

  protected def outgoingStage: OperationStage[Output, Output]

  protected def endpointStage: OperationStage[Input, Output]

  lazy val requestProcessingGraph
    : Graph[FlowShape[(Input, RouteStatus), Output], _] =
    GraphDSL
      .create() { implicit builder =>
        import GraphDSL.Implicits._
        import OperationStage._

        val input = builder.add(inputStage)

        val incoming = builder.add(incomingStage.buildGraph)
        val outgoing = builder.add(outgoingStage.buildGraph)
        val endpoint = builder.add(endpointStage.buildGraph)

        val responseMerge = builder.add(responseMergeStage)

        val incomingOut = fromOutletResult(incoming.out(1))
        val endpointOut = fromOutletResult(endpoint.out(1))
        val outgoingOut = fromOutletResult(outgoing.out(1)).map(_._1)

        // format: off
        input ~> incoming.in
                 incoming.out(0) ~> abortResponseFlow ~> responseMerge.in(0)
                 incomingOut ~> endpoint.in
                                endpoint.out(0) ~> abortResponseFlow ~> responseMerge.in(1)
                                endpointOut ~> outgoing.in
                                               outgoing.out(0) ~> abortResponseFlow ~> responseMerge.in(2)
                                               outgoingOut ~> responseMerge.in(3)
        // format: on

        FlowShape(input.in, responseMerge.out)
      }
      .named("processing-flow")

  private lazy val inputStage = Flow[(Input, RouteStatus)]
  private lazy val responseMergeStage = Merge[Output](4)
  private lazy val abortResponseFlow =
    Flow[(StageResult[_], RouteStatus)]
      .map {
        case (stageResult, _) =>
          stageResult match {
            case Failed(cause) => HttpResponse(entity = cause.cause)
            case _ => throw new MatchError("Abort flow excepts failed result.")
          }
      }
}
