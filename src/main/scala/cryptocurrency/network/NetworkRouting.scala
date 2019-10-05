package cryptocurrency.network

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, path}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cryptocurrency.blockchain.BlockChain
import cryptocurrency.network.actors.MineActor.{GetBlockChain, MessageMine, VerifyIntegrity}
import cryptocurrency.mining.JsonSupport._

trait NetworkRouting extends SprayJsonSupport {

  implicit val executionContext: ExecutionContext
  implicit lazy val timeout: Timeout = 5.seconds
  val actor: ActorRef

  val route: Route =
    get {
      path("mine") {
        val chain: Future[MessageMine] = (WebServer.actor ? MessageMine).mapTo[MessageMine]
        onSuccess(chain) { status =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Computed hash: ${status.string}"))
        }
      } ~
      path("blockchain") {
        val chain: Future[BlockChain] = (WebServer.actor ? GetBlockChain).mapTo[BlockChain]
        onSuccess(chain) { status =>
          complete(StatusCodes.OK, status)
        }
      } ~
      path("verify") {
        val chain: Future[VerifyIntegrity] = (WebServer.actor ? VerifyIntegrity).mapTo[VerifyIntegrity]
        onSuccess(chain) { status =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Chain Integrity Status: ${ if(status.status) "Valid" else "Invalid" }"))
        }
      }
    }
}
