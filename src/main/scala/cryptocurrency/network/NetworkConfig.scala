package cryptocurrency.network

import com.typesafe.config.{Config, ConfigFactory}

object NetworkConfig {
  val config: Config = ConfigFactory.load()

  implicit val httpHost: String = config.getString("http.host")
  implicit val httpPort: Int = config.getInt("http.port")

  implicit val miningDifficultyIncreaseRate: Int = config.getInt("mining.difficulty-increase-rate")
  implicit val blockReward: Int = config.getInt("mining.block-reward")
}
