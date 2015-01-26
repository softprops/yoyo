package yoyo

import com.ning.http.client.Response
import dispatch.as
import org.json4s._

case class Delivery(success: Boolean, id: String)

case class Count(subscribers: Long)

sealed trait Exists
object Exists {
  case object Yep extends Exists 
  case object Nope extends Exists
}

case class Account(
  id: String,
  username: String,
  apiToken: String,
  bio: Option[String],
  callback: Option[String],
  apiUser: Boolean,
  subscribable: Boolean,
  name: Option[String],
  needsLoc: Boolean,
  photo: Option[String],
  tok: String,
  welcomeLink: Option[String])


trait Rep[T] {
  def map(value: Response): T
}

object Rep {
  implicit val identity: Rep[Response] =
    new Rep[Response] {
      def map(value: Response) = value
    }

  implicit val delivery: Rep[Delivery] =
    new Rep[Delivery] {
      def map(value: Response) = (for {
        JObject(success)        <- as.json4s.Json(value)
        ("success", JBool(suc)) <- success
        ("yo_id", JString(id))  <- success
      } yield Delivery(suc, id)).head
    }

  implicit val count: Rep[Count] =
    new Rep[Count] {
      def map(value: Response) = (for {
        JObject(c) <- as.json4s.Json(value)
        ("count", JInt(count)) <- c
      } yield Count(count.toLong)).head
    }

  implicit val exists: Rep[Exists] = 
    new Rep[Exists] {
      def map(value: Response) = (for {
        JObject(answer)           <- as.json4s.Json(value)
        ("exists", JBool(exists)) <- answer
      } yield exists match {
        case true  => Exists.Yep
        case false => Exists.Nope
      }).head
    }

  implicit val account: Rep[Account] =
    new Rep[Account] {
      def optStr(value: JValue) = value match {
        case JString(str) => Some(str)
        case _ => None
      }
      def map(value: Response) = (for {
        JObject(a)                          <- as.json4s.Json(value)
        ("api_token", JString(apiToken))    <- a
        ("bio", bio)                        <- a
        ("callback", cb)                    <- a
        ("is_api_user", JBool(apiUser))     <- a
        ("is_subscribable", JBool(sub))     <- a
        ("name", name)                      <- a
        ("needs_location", JBool(needsLoc)) <- a
        ("photo", photo)                    <- a
        ("tok", JString(tok))               <- a
        ("user_id", JString(id))            <- a
        ("username", JString(username))     <- a
        ("welcome_link", welcome)           <- a
      } yield Account(
        id,
        username,
        apiToken,
        optStr(bio),
        optStr(cb),
        apiUser,
        sub,
        optStr(name),
        needsLoc,
        optStr(photo),
        tok,
        optStr(welcome))).head        
    }
}
