package yoyo

import com.ning.http.client.Response
import dispatch.{ as, Http, Req, :/ }
import org.json4s.{ JObject, JInt, JString }
import scala.concurrent.{ ExecutionContext, Future }

/** justyo - http://dev.justyo.co/yo/documents.html */
case class Yo(
  token: String, http: Http = new Http)
 (implicit ec: ExecutionContext) {
  private[this] val credentials = Map("api_token" -> token)
  private[this] def base = :/("api.justyo.co")
  private[this] def sign(req: Req): Req =
    if ("GET" == req.toRequest.getMethod) req <<? credentials
    else req << credentials
  private[this] def request[T](req: Req)(transform: Response => T) = http(
    sign(req / "") OK transform
  )

  def all = request(base.POST / "yoall") (as.json4s.Json andThen {
    _ => true
  })

  def yo(username: String) = request(
    base.POST / "yo" << Map("username" -> username)
  ) (as.json4s.Json andThen {
    for {
      JObject(fields)          <- _
      ("result", JString(res)) <- fields
    } yield "OK" == res
  })

  def subscribers = request(
    base / "subscribers_count"
  ) (as.json4s.Json andThen {
    for {
      JObject(fields)         <- _
      ("result", JInt(count)) <- fields
    } yield count.toInt
  })

  def close() = http.shutdown()
}
