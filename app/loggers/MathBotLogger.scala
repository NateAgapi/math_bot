package loggers

import com.google.inject.Singleton
import play.api.Logger

@Singleton
class MathBotLogger {
  private def logger(who: String) = Logger(s"[$who]")

  def LogFailure(who: String, msg: String) = logger(who).warn(msg)

  def LogInfo(who: String, msg: String) = logger(who).info(msg)

  def LogDebug(who: String, msg: String) = logger(who).debug(msg)
}
