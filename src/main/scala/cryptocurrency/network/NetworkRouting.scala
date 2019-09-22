package cryptocurrency.network

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import cryptocurrency.network.NetworkActor.MessageMine
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait NetworkRouting {

  implicit val executionContext: ExecutionContext
  implicit val timeout: Timeout = 5.seconds

  val route: Route =
    get {
      path("mine") {
        val chain: Future[MessageMine] = (WebServer.actor ? MessageMine).mapTo[MessageMine]

        onSuccess(chain) { status =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Computed hash: ${status.string}"))
        }
      }
    }
}
