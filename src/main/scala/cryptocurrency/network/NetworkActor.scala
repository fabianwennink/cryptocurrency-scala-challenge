package cryptocurrency.network

import akka.actor.{Actor, Props}
import akka.util.Timeout
import cryptocurrency.mining.Miner

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class NetworkActor extends Actor {
  import NetworkActor._

  implicit val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  def receive: Receive = {
    case MessageMine => sender() ! MessageMine(Miner.createHash(System.currentTimeMillis()))
  }
}

object NetworkActor {
  def props: Props = Props[NetworkActor]

  case class MessageMine(string: String)

  case object MessageMine
}
