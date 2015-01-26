package yoyo

import com.ning.http.client.{ AsyncHandler, Response }
import dispatch.{ FunctionHandler, Http, Req, :/ }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NoStackTrace
import java.util.concurrent.ExecutionException

object Yo {
  type Handler[T] = AsyncHandler[T]

  case class Error(code: Int, message: String)
    extends RuntimeException(message) with NoStackTrace

  abstract class Completion[T: Rep]
    (implicit ec: ExecutionContext) {

    def apply[TT]
      (handler: Handler[TT]): Future[TT]

    def apply(): Future[T] =
      apply(implicitly[Rep[T]].map(_))

    def apply[TT]
      (f: Response => TT): Future[TT] =
        apply(new FunctionHandler(f) {
          override def onCompleted(response: Response) = {
            if (response.getStatusCode / 100 == 2) f(response)
            else throw Error(
              response.getStatusCode,
              if (response.hasResponseBody) response.getResponseBody else "")
          }
        }).recoverWith {
          case ee: ExecutionException =>
            Future.failed(ee.getCause)
        }
  }
}

/** justyo - http://dev.justyo.co/yo/documents.html */
case class Yo(
  token: String, http: Http = new Http)
 (implicit ec: ExecutionContext) {

  private[this] val credentials =
    Map("api_token" -> token)

  private[this] val agent =
    Map("User-Agent" -> s"yoyo/${BuildInfo.version}")

  private[this] def base = :/("api.justyo.co")

  private def complete[A: Rep]
   (req: Req): Yo.Completion[A] =
    new Yo.Completion[A] {
      def apply[T](hand: Yo.Handler[T]) =
        request(req)(hand)
    }

  private def sign(req: Req): Req =
    if ("GET" == req.toRequest.getMethod)
      req <<? credentials
    else req << credentials

  def request[T]
   (req: Req)
   (hand: Yo.Handler[T]) =
     http(sign(req / "")
          <:< agent
          > hand)

  object yo {
    case class Envelope(
      username: Option[String]                        = None,
      private val _link: Option[String]               = None,
      private val _location: Option[(Double, Double)] = None)
      extends Yo.Completion[Delivery] {

      def link(l: String) =
        copy(_link = Some(l))

      def location(lat: Double, lon: Double) =
        copy(_location = Some((lat, lon)))

      def apply[T](hand: Yo.Handler[T]): Future[T] =
        request(
          base.POST /
          username.map(_ => "yo").getOrElse("yoall")
          << username.map(("username" -> _))
            ++ _link.map(("link" -> _))
            ++ _location.map {
              case (lat, lon) => ("location" -> s"$lat,$lon")
            })(hand)
    }

    def user(name: String) = Envelope(Some(name))

    def all = Envelope()
  }

  object subscriber {
    def count =
      complete[Response](base / "subscribers_count")
  }

  object account {
    case class Create(
      name: String, passcode: String,
      private val _callback: Option[String]       = None,
      private val _email: Option[String]          = None,
      private val _description: Option[String]    = None,
      private val _needsLocation: Option[Boolean] = None)
    extends Yo.Completion[Response] {

      def callback(cb: String) =
        copy(_callback = Some(cb))

      def email(addr: String) =
        copy(_email = Some(addr))

      def description(desc: String) =
        copy(_description = Some(desc))

      def needsLocation(needs: Boolean) =
        copy(_needsLocation = Some(needs))

      def apply[T](hand: Yo.Handler[T]): Future[T] =
        request(
          base.POST / "accounts"
          << Map(
            "new_account_username" -> name.toUpperCase,
            "new_account_passcode" -> passcode)
            ++ _callback.map(("callback_url" -> _))
            ++ _email.map(("email" -> _))
            ++ _description.map(("description" -> _))
            ++ _needsLocation.map(("needs_location" -> _.toString)))(hand)
    }

    def create(name: String, password: String) =
      Create(name, password)

    def exists(name: String) =
      complete[Exists](
        base / "check_username"
        <<? Map("username" -> name))      
  }

  def close() = http.shutdown()
}
