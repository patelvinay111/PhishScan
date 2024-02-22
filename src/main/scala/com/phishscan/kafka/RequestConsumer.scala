package com.phishscan.kafka

import monix.kafka._
import monix.kafka.config.{AutoOffsetReset, SecurityProtocol}
import monix.reactive.Observable

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

/** Wrapper class for Kafka Consumer used to consume request messages from a Kafka Queue
 * @param kafkaServer
 *   Used as the bootstrap.servers setting and represents the list of servers to connect to
 * @param kafkaGroupId
 *   Determines which consumers belong to which group
 * @param kafkaTopic
 *   The topic these records should be fetched from
 * @param autoOffsetReset
 *   Used to determine the offset behavior of the Kafka consumer
 */
class RequestConsumer(
                           kafkaServer: String,
                           kafkaGroupId: String,
                           kafkaTopic: String,
                           autoOffsetReset: AutoOffsetReset,
                           kafkaSecurityProtocol: SecurityProtocol
                         ) {
  private val consumerCfg = KafkaConsumerConfig.default.copy(
    bootstrapServers = List(kafkaServer),
    groupId = kafkaGroupId,
    autoOffsetReset = autoOffsetReset,
    securityProtocol = kafkaSecurityProtocol,
    sessionTimeout = FiniteDuration(30000, MILLISECONDS)
  )

  private val consumer = KafkaConsumerObservable[String, String](consumerCfg, List(kafkaTopic))

  val requestMessages: Observable[String] =
    consumer.map(_.value())
}