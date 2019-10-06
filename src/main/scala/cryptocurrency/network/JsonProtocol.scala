package cryptocurrency.network

import cryptocurrency.blockchain.{Block, BlockChain, GenesisBlock}
import spray.json._

object JsonProtocol extends DefaultJsonProtocol {

  implicit object BlockJsonFormat extends RootJsonFormat[Block] {
    def read(json: JsValue): Block = json.asJsObject.getFields("index", "hash", "nonce", "difficulty", "reward", "timestamp", "previous") match {
      case Seq(JsNumber(index), JsString(previousHash), JsNumber(nonce), JsNumber(difficulty), JsNumber(reward), JsNumber(timestamp), tail) =>
        Block(index.toInt, previousHash, nonce.toLong, difficulty.toInt, reward.toInt, timestamp.toLong, tail.convertTo(BlockChainJsonFormat))
      case _ => throw DeserializationException("Invalid Block")
    }

    def write(block: Block): JsValue = JsObject(
      "index" -> JsNumber(block.index),
      "hash" -> JsString(block.previousHash),
      "nonce" -> JsNumber(block.nonce),
      "difficulty" -> JsNumber(block.difficulty),
      "reward" -> JsNumber(block.reward),
      "timestamp" -> JsNumber(block.timestamp),
      "previous" -> BlockChainJsonFormat.write(block.previous)
    )
  }

  implicit object BlockChainJsonFormat extends RootJsonFormat[BlockChain] {
    def read(json: JsValue): BlockChain = {
      json.asJsObject.getFields("hash") match {
        case Seq(_) => json.convertTo[Block]
        case Seq() => GenesisBlock
      }
    }

    def write(chain: BlockChain): JsValue = chain match {
      case block: Block => block.toJson
      case GenesisBlock => JsObject(
        "index" -> JsNumber(GenesisBlock.index),
        "hash" -> JsString(GenesisBlock.hash),
        "nonce" -> JsNumber(GenesisBlock.nonce),
        "difficulty" -> JsNumber(GenesisBlock.difficulty),
        "reward" -> JsNumber(GenesisBlock.reward),
        "timestamp" -> JsNumber(GenesisBlock.timestamp)
      )
    }
  }
}
