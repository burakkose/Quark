package io.github.quark.stage

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Host
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import akka.stream.{ActorMaterializer, FlowShape, Graph}
import io.github.quark.action.GatewayAction
import io.github.quark.action.OperationAction.{Endpoint, Incoming, Outgoing}
import io.github.quark.resolver.ServiceResolver
import io.github.quark.route.RouteStatus
import io.github.quark.stage.PipelineStage.{Input, Output}

import scala.concurrent.Future

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
            val notFound = builder.add(notFoundFlow)
            val found = builder.add(requestProcessingGraph)

            // format: off
            input ~> partition.in
                     partition.out(1) ~> found    ~> merge.in(1)
                     partition.out(0) ~> notFound ~> merge.in(0)
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

  def apply(gateway: GatewayAction, serviceResolver: ServiceResolver)(
      implicit actorSystem: ActorSystem,
      actorMaterializer: ActorMaterializer): PipelineStage =
    new PipelineStage with RequestProcessingStage {

      implicit val executionContext = actorSystem.dispatcher

      protected def partitionFn: ((Input, RouteStatus)) => Int =
        tup =>
          tup._2 match {
            case RouteStatus.Matched(_) => 1
            case RouteStatus.UnMatched => 0
        }

      protected def findServiceFn: (Input) => RouteStatus =
        req => gateway.service(req)

      protected val incomingStage: OperationStage[Input, Input] =
        new OperationStage[Input, Input]("incoming-flow") {
          protected val defaultOperation = Incoming(
            req => Future.successful(Right(req))
          )
        }

      protected val endpointStage: OperationStage[Input, Output] =
        new OperationStage[Input, Output]("endpoint-flow") {
          protected val defaultOperation = Endpoint { req =>
            serviceResolver.findServiceLocation(req) match {
              case Some((remote, path)) =>
                val requestURI = req.uri
                val uriAuthority = requestURI.authority
                val modifiedURI = requestURI.copy(
                  authority = uriAuthority.copy(host = Host(remote), port = 0),
                  path = Uri.Path(path)
                )
                Http()
                  .singleRequest(req.copy(uri = modifiedURI))
                  .map(Right.apply)
              case _ =>
                Future.successful(Left(s"${req.uri} cannot be resolved."))
            }
          }
        }

      protected val outgoingStage: OperationStage[Output, Output] =
        new OperationStage[Output, Output]("outgoing-flow") {
          protected val defaultOperation = Outgoing(
            res => Future.successful(Right(res))
          )
        }
    }
}
