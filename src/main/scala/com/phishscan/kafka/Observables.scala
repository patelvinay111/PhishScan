package com.phishscan.kafka

import monix.kafka.config.{AutoOffsetReset, SecurityProtocol}
import monix.reactive.Observable

object Observables {

  /** Used to produce a monix observable of FileRequestedMessages coming off a Kafka Queue
   * @param kafkaServer
   *   Used as the bootstrap.servers setting and represents the list of servers to connect to
   * @param kafkaGroupId
   *   Determines which consumers belong to which group
   * @param kafkaTopicIn
   *   The topic these records should be fetched from
   * @param autoOffsetReset
   *   Used to determine the offset behavior of the Kafka consumer
   * @return
   */
  def kafkaBacked(
                   kafkaServer: String,
                   kafkaGroupId: String,
                   kafkaTopicIn: String,
                   autoOffsetReset: AutoOffsetReset,
                   kafkaSecurityProtocol: SecurityProtocol
                 ): Observable[String] = {
    new RequestConsumer(kafkaServer, kafkaGroupId, kafkaTopicIn, autoOffsetReset, kafkaSecurityProtocol).requestMessages
  }
}
