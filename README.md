# yoyo

> just [yo](http://www.justyo.co/).

## usage

The entrypoint for yoyo is a class called... `Yo`. In order to interact with yo you need to 
grab an api key from an account listed [here](http://dev.justyo.co/).

All api interaction
with yo happens asyncronously so an implicit `ExecutionContext` should be in scope.


```scala
import scala.concurrent.ExecutionContext.Implicits.global

val cli = yoyo.Yo(token)
```

### yo(ing)

The core use of you is to send a "yo" to a user or to all yo users subscribed to your account

Given a name like "foo", you can yo them with

```scala
import scala.concurrent.Future
import yoyo.Delivery
val delivery: Future[Delivery] = cli.yo.user("foo")()
```

Alternatively you can "yo" at all of the users subscribed to the user owning a given api key

```scala
val deliever: Future[Delivery] = cli.yo.all()
```

If you wish to delivery more than just a "yo" you can send a link or a location in lat, lon format.

```scala
val loc: Future[Delivery] =
  cli.yo.user("foo").location(lat, lon)()

val link: Future[Delivery] = 
  cli.yo.user("foo").link("https://github.com/softprops/yoyo#readme")()
```

### subscribers

You can know your current subscriber count in the [dev dashboard](http://dev.justyo.co/) but you can also know programatically

```scala
val count: Future[Count] =
  cli.subscriber.count()
```

### accounts

From the [dev dashboard](http://dev.justyo.co/) you can create new api accounts. You can also do this programatically

Before attempting to create an account you can check the username is available first

```scala
import yoyo.Exists

val exists = Future[Exists] =
  cli.account.exists("foobar")

exists.map {
  case Exists.Yep =>  // it exists
  case Exists.Nope => // it does not exist
}
```

If a username does not already exist you can create a new dev account with

```scala
import yoyo.Account
val account: Future[Account] =
  cli.account.create("foobar", "passcode")()
```

Note, this account will show up in your [dev dashboard](http://dev.justyo.co/) afterwards

Doug Tangren (softprops) 2015
