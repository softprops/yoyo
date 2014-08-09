package yoyo

import scala.concurrent.ExecutionContext
import dispatch._
import dispatch.{ as, Http, Req, :/ }

case class Yo(
  token: String, http: Http = new Http)
 (implicit ec: ExecutionContext) {
  private[this] def base = :/("api.justyo.com")
  private[this] def request(req: Req) = http(
    req <<? Map("api_token" -> token) OK as.String
  )
  def all = request(base.POST / "yoall")
  def yo(username: String) = request(
    base.POST / "yo" <<? Map("username" -> username)
  )
  def subscribers = request(base / "subscribers_count")
}
