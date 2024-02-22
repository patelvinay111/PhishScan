package com.phishscan.kafka

import com.phishscan.schema.{Email, ValidatedEmailResponse}
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable

import scala.concurrent.duration.DurationInt

object StreamingProgram extends LazyLogging {

  def build(
             observable: Observable[String],
             processEmail: Email => ValidatedEmailResponse
           )(implicit scheduler: Scheduler): Task[Unit] = {
    logger.debug("StreamingProgram Build initiated")

    val processed: Observable[Seq[ValidatedEmailResponse]] = observable
      .map(Serde.deserializeRequested)
      .map(processEmail)
      .bufferTimedAndCounted(maxCount = 10, timespan = 5.seconds)

    Task(processed)
  }

}
