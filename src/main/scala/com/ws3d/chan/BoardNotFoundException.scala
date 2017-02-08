package com.ws3d.chan

object HttpExceptions {
  case class BoardNotFoundException(message: String) extends RuntimeException(message)
  case class ServerException(message: String) extends RuntimeException(message)
}
