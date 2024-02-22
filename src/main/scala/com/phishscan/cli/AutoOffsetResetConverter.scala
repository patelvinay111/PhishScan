package com.phishscan.cli

import com.typesafe.config.ConfigException.BadValue
import monix.kafka.config.AutoOffsetReset
import picocli.CommandLine.{ITypeConverter, TypeConversionException}

class AutoOffsetResetConverter extends ITypeConverter[AutoOffsetReset] {
  override def convert(value: String): AutoOffsetReset = {
    try {
      AutoOffsetReset(value)
    } catch {
      case _: BadValue =>
        throw new TypeConversionException(
          s"""Invalid selection: AutoOffsetReset must be either "Latest" or "Earliest" but was "${value}""""
        )
    }
  }
}
