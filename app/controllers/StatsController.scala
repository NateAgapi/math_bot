package controllers

import java.net.URLDecoder
import javax.inject.Inject

import actors.messages.ActorFailed
import actors.StatsActor._
import actors.StatsActor
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern.ask
import loggers.MathBotLogger
import model.models.Stats
import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class StatsController @Inject()(system: ActorSystem, val reactiveMongoApi: ReactiveMongoApi, logger: MathBotLogger)
    extends Controller {

  implicit val timeout: Timeout = 5000.minutes

  val statsActor = system.actorOf(StatsActor.props(system, reactiveMongoApi, logger), "stats-actor")

  def advanceStats(encodedTokenId: String, success: Option[String]) = Action.async {
    implicit request: Request[AnyContent] =>
      (statsActor ? UpdateStats(success.contains("true"), URLDecoder.decode(encodedTokenId, "UTF-8")))
        .mapTo[Either[Stats, ActorFailed]]
        .map {
          case Left(stats) => Ok(Json.prettyPrint(Json.toJson(stats)))
          case Right(invalidJson) => BadRequest(invalidJson.msg)
        }
  }

  def getStats(encodedTokenId: String) = Action.async { implicit request =>
    (statsActor ? GetStats(URLDecoder.decode(encodedTokenId, "UTF-8")))
      .mapTo[Either[Stats, ActorFailed]]
      .map {
        case Left(stats) => Ok(Json.toJson(stats))
        case Right(invalidJson) => BadRequest(invalidJson.msg)
      }
  }

  def changeLevel(encodedTokenId: String, level: String, step: String) = Action.async { implicit request =>
    (statsActor ? ChangeLevel(URLDecoder.decode(encodedTokenId, "UTF-8"), level, step))
      .mapTo[Either[Stats, ActorFailed]]
      .map {
        case Left(stats) => Ok(Json.toJson(stats))
        case Right(invalidJson) => BadRequest(invalidJson.msg)
      }
  }

  def unlock(encodedTokenId: String) = Action.async { implicit request: Request[AnyContent] =>
    (statsActor ? Unlock(URLDecoder.decode(encodedTokenId, "UTF-8")))
      .mapTo[Either[Stats, ActorFailed]]
      .map {
        case Left(stats) => Ok(Json.toJson(stats))
        case Right(invalidJson) => BadRequest(invalidJson.msg)
      }
  }

  def reset(encodedTokenId: String) = Action.async { implicit request: Request[AnyContent] =>
    (statsActor ? Reset(URLDecoder.decode(encodedTokenId, "UTF-8"))).mapTo[Either[Stats, ActorFailed]].map {
      case Left(stats) => Ok(Json.prettyPrint(Json.toJson(stats)))
      case Right(invalidJson) => BadRequest(invalidJson.msg)
    }
  }
}
