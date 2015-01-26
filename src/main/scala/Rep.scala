package yoyo

import com.ning.http.client.Response
import dispatch.as
import org.json4s._

case class Delivery(success: Boolean, id: String)

sealed trait Exists
object Exists {
  case object Yep extends Exists 
  case object Nope extends Exists
}

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
}
