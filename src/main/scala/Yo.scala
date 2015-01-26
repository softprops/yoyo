package yoyo

import com.ning.http.client.{ AsyncHandler, Response }
import dispatch.{ as, FunctionHandler, Http, Req, :/ }
import org.json4s._
import scala.concurrent.{ ExecutionContext, Future }

trait Rep[T] {
  def map(value: Response): T
}

object Rep {
  implicit val identity: Rep[Response] =
    new Rep[Response] {
      def map(value: Response) = value
    }
}

object Yo {
  type Handler[T] = AsyncHandler[T]

  abstract class Completion[T: Rep] {
    def apply[TT]
      (handler: Handler[TT]): Future[TT]
    def apply(): Future[T] =
      apply(implicitly[Rep[T]].map(_))
    def apply[TT]
      (f: Response => TT): Future[TT] =
        apply(new FunctionHandler(f))
  }
}

/** justyo - http://dev.justyo.co/yo/documents.html */
case class Yo(
  token: String, http: Http = new Http)
 (implicit ec: ExecutionContext) {

  private[this] val credentials =
    Map("api_token" -> token)

  private[this] def base = :/("api.justyo.co")

  def complete[A: Rep]
   (req: Req): Yo.Completion[A] =
    new Yo.Completion[A] {
      def apply[T](hand: Yo.Handler[T]) =
        request(req)(hand)
    }

  def sign(req: Req): Req =
    if ("GET" == req.toRequest.getMethod)
      req <<? credentials
    else req << credentials

  def request[T]
   (req: Req)
   (hand: Yo.Handler[T]) =
     http(sign(req / "") > hand)

  object yo {
    case class User(
      name: String,
      _link: Option[String]           = None,
      _location: Option[(Long, Long)] = None)
      extends Yo.Completion[Response] {
      def link(l: String) = copy(_link = Some(l))
      def location(lat: Long, lon: Long) = copy(_location = Some((lat, lon)))
      def apply[T](hand: Yo.Handler[T]): Future[T] =
        request(base.POST / "yo" << Map("username" -> name))(hand)
    }

    case class All(link: Option[String] = None)
      extends Yo.Completion[Response] {
      def apply[T](hand: Yo.Handler[T]): Future[T] =
        request(base.POST / "yoall")(hand)
    }

    def user(name: String) = User(name)

    def all = All()
  }

  object subscriber {
    def count =
      complete[Response](base / "subscribers_count")
  }

  object account {
    case class Create(
      name: String, passcode: String,
      callback: Option[String]       = None,
      email: Option[String]          = None,
      description: Option[String]    = None,
      needsLocation: Option[Boolean] = None)
    extends Yo.Completion[Response] {
      def apply[T](hand: Yo.Handler[T]): Future[T] =
        request(base.POST / "accounts"
                << Map(
                  "new_account_username" -> name,
                  "new_account_passcode" -> passcode)
                  ++ callback.map(("callback_url" -> _))
                  ++ email.map(("email" -> _))
                  ++ description.map(("description" -> _))
                  ++ needsLocation.map(("needs_location" -> _.toString)))(hand)
    }

    def create(name: String, password: String) =
      Create(name, password)

    def exists(name: String) =
      complete[Response](
        base / "check_username"
        <<? Map("username" -> name))      
  }

  def close() = http.shutdown()
}
