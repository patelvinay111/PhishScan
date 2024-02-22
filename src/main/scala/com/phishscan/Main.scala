package com.phishscan

import com.phishscan.cli.{AutoOffsetResetConverter, SecurityProtocolConverter}
import com.phishscan.kafka.{Observables, StreamingProgram}
import com.phishscan.schema.{Email, ValidatedEmailResponse}
import com.typesafe.scalalogging.LazyLogging
import monix.execution.Scheduler
import monix.kafka.config.{AutoOffsetReset, SecurityProtocol}
import okhttp3.{MediaType, OkHttpClient, Request, RequestBody, Response}
import picocli.CommandLine
import picocli.CommandLine.Option

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends LazyLogging{
  object PhishScan extends Runnable {
    @Option(
      names = Array("--kafka-server"),
      required = true,
      description = Array("""The location of the Kafka Server to use"""),
      defaultValue = "127.0.0.1:9092"
    )
    private var kafkaServer: String = _

    @Option(
      names = Array("--kafka-topic-in"),
      required = true,
      description = Array("""The name of the topic read in"""),
      defaultValue = "email_stream"
    )
    private var kafkaTopicIn: String = _

    @Option(
      names = Array("--kafka-topic-in-group"),
      required = true,
      description = Array("""The ID of the kafka group for the topic read in"""),
      defaultValue = "default"
    )
    private var kafkaGroupId: String = _

    @Option(
      names = Array("--auto-offset-reset"),
      required = true,
      description = Array("""Used to determine the offset behavior of the Kafka consumer. Defaulted to "Latest"."""),
      defaultValue = "Latest",
      converter = Array(classOf[AutoOffsetResetConverter])
    )
    private var autoOffsetReset: AutoOffsetReset = _

    @Option(
      names = Array("--kafka-security-protocol"),
      required = true,
      description = Array("""The security protocol to use to connect to Kafka"""),
      defaultValue = "SSL",
      converter = Array(classOf[SecurityProtocolConverter])
    )
    private var kafkaSecurityProtocol: SecurityProtocol = _

    private def readAndProcess(emailText: Email): ValidatedEmailResponse = {
      val client = new OkHttpClient.Builder().build()

      val mediaType = MediaType.parse("application/json")
      val body = RequestBody.create(mediaType,
        """
          |{
          | "contents": [
          |  {
          |   "parts": [
          |    {
          |     "text": "Is the following sentence positive sentiment or negative\n\nPasta was cold"
          |    }
          |   ]
          |  }
          | ],
          | "generationConfig": {
          |  "temperature": 0,
          |  "topK": 1,
          |  "topP": 0.1,
          |  "maxOutputTokens": 2048,
          |  "stopSequences": []
          | },
          | "safetySettings": [
          |  {
          |   "category": "HARM_CATEGORY_HARASSMENT",
          |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
          |  },
          |  {
          |   "category": "HARM_CATEGORY_HATE_SPEECH",
          |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
          |  },
          |  {
          |   "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
          |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
          |  },
          |  {
          |   "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
          |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
          |  }
          | ]
          |}""".stripMargin)

      val request = new Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent?key=AIzaSyAv2BDoSXQClkmArzJe-uNBxCJXohxBmds")
        .method("POST", body)
        .addHeader("Content-Type", "application/json")
        .build()

      val response: Response = client.newCall(request).execute()

      // Handle the response as needed
      println(response.body().string())

      // Close the response to release resources
      response.close()

      ValidatedEmailResponse(true) //Placeholder
    }

    override def run(): Unit = {
      logger.info("Waiting to receive messages to process")
      implicit val scheduler: Scheduler = monix.execution.Scheduler.global

      logger.debug("Environment variables set as follows:")
      logger.debug(s"Kafka Server: $kafkaServer")
      logger.debug(s"Kafka Security Protocol: $kafkaSecurityProtocol")
      logger.debug(s"Kafka Topic in: $kafkaTopicIn")
      logger.debug(s"Kafka Group ID: $kafkaGroupId")
      logger.debug(s"Auto Offset Reset: $autoOffsetReset")

      Await.result(
        StreamingProgram
          .build(
            observable = Observables.kafkaBacked(kafkaServer, kafkaGroupId, kafkaTopicIn, autoOffsetReset, kafkaSecurityProtocol),
            processEmail = readAndProcess
          )
          .runToFuture,
        Duration.Inf
      )
    }
  }

  private lazy val commandLine: CommandLine = new CommandLine(PhishScan)
  def main(args: Array[String]): Unit = {
    logger.info("Application started.")
    val exitCode = commandLine.execute(args: _*)
    if (exitCode != 0) throw new RuntimeException(s"Process exited with status code $exitCode")
  }
}
