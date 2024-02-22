package com.phishscan.cli

import com.typesafe.config.ConfigException.BadValue
import monix.kafka.config.SecurityProtocol
import picocli.CommandLine.{ITypeConverter, TypeConversionException}

class SecurityProtocolConverter extends ITypeConverter[SecurityProtocol] {
  override def convert(value: String): SecurityProtocol = {
    try {
      SecurityProtocol(value)
    } catch {
      case _: BadValue =>
        throw new TypeConversionException(
          s"""Invalid selection: $value is not a valid monix.kafka.config.SecurityProtocol value"""
        )
    }
  }
}
