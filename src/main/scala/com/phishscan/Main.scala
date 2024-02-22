package com.phishscan

import com.phishscan.cli.{AutoOffsetResetConverter, SecurityProtocolConverter}
import com.phishscan.kafka.{Observables, StreamingProgram}
import com.phishscan.schema.{Email, ValidatedEmailResponse}
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser
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

    @Option(
      names = Array("--gemini-token"),
      required = true,
      description = Array("""Google Gemini Api Token""")
    )
    private var geminiToken: String = _

    private def readAndProcess(email: Email): ValidatedEmailResponse = {
      val emailContent = email.content
      val client = new OkHttpClient.Builder().build()

      val mediaType = MediaType.parse("application/json")
      val body = RequestBody.create(mediaType,
        s"""
          |{
          | "contents": [
          |  {
          |   "parts": [
          |    {
          |     "text": "Consider the following email content and and true if this is likely a phishing email, false otherwise\n$emailContent"
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
        .url(s"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent?key=$geminiToken")
        .method("POST", body)
        .addHeader("Content-Type", "application/json")
        .build()

      val response: Response = client.newCall(request).execute()

      // Handle the response as needed
      val isPhishing: Boolean = parser
        .parse(response.body().string())
        .flatMap(
          _.hcursor.downField("candidates").downArray
            .downField("content")
            .downField("parts").downArray
            .downField("text").as[String]) match {
        case Right(value) => value.toBoolean
        case Left(_)  => true // Assume Phishing if ran into error
      }

      // Close the response to release resources
      response.close()

      ValidatedEmailResponse(isPhishing)
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
