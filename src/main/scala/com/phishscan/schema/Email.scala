package com.phishscan.schema

import io.circe._
import io.circe.generic.semiauto._

case class Email(content: String)

object Email {
  implicit val encoder: Encoder[Email] = deriveEncoder[Email]
  implicit val decoder: Decoder[Email] = deriveDecoder[Email]
}
