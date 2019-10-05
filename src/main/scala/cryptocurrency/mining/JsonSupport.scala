package cryptocurrency.mining

import cryptocurrency.blockchain.{Block, BlockChain, GenesisBlock}
import spray.json._

object JsonSupport extends DefaultJsonProtocol {

  implicit object BlockJsonFormat extends RootJsonFormat[Block] {
    def read(json: JsValue): Block = json.asJsObject.getFields("index", "hash", "nonce", "timestamp", "previous") match {
      case Seq(JsNumber(index), JsString(previousHash), JsNumber(nonce), JsNumber(timestamp), tail) =>
        Block(index.toInt, previousHash, nonce.toLong, timestamp.toLong, tail.convertTo(BlockChainJsonFormat))
      case _ => throw DeserializationException("Invalid Block")
    }

    def write(block: Block): JsValue = JsObject(
      "index" -> JsNumber(block.index),
      "hash" -> JsString(block.previousHash),
      "nonce" -> JsNumber(block.nonce),
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
        "timestamp" -> JsNumber(GenesisBlock.timestamp)
      )
    }
  }
}
