package com.phishscan.kafka

import com.phishscan.schema.Email
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser.decode

object Serde extends LazyLogging {
  def deserializeRequested(msg: String): Email = {
    logger.debug(s"Deserializing: $msg")
    val deserialized = decode[Email](msg)
    deserialized match {
      case Left(error) =>
        logger.error(s"Deserialization error: $error")
        throw error
      case Right(result) =>
        logger.debug(s"Deserialized: $result")
        result
    }
  }
}
