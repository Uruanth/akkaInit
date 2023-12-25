package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.pipe
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import part6patterns.AskSpec.AuthManager.{AUTH_FAIL, AUTH_FAIL_PASS, AUTH_FAIL_SYSTEM}

import scala.concurrent.ExecutionContext
import scala.language.{existentials, postfixOps}
import scala.util.{Failure, Success}

// step 1 - import patter
import akka.pattern.ask

import scala.concurrent.duration._

class AskSpec extends
  TestKit(ActorSystem(
    "AskSpec",
    ConfigFactory
      .load()
      .getConfig("interceptingLogMessages")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import AskSpec._

  "An authenticator" should {
    import AuthManager._
    "fail to authenticate a non-registered user" in {
      val authManager = system.actorOf(Props[PipedAuthManager])
      authManager ! Authenticate("Dairon", "rrets")
      expectMsg(AuthFailure(AUTH_FAIL))
    }

    "find user" in {
      val authManager = system.actorOf(Props[PipedAuthManager])
      authManager ! RegisterUser("Dairon", "klbd")
      authManager ! Authenticate("Dairon", "klsbd")
      expectMsg(AuthFailure(AUTH_FAIL_PASS))

    }

  }

}

object AskSpec {

  case class Read(key: String)

  case class Write(key: String, value: String)

  class KVActor extends Actor with ActorLogging {

    override def receive: Receive = online(Map())


    private def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Trying to read the value at the key $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"Writing the value $value for the key $key")
        context.become(online(kv + (key -> value)))
    }

  }


  case class RegisterUser(username: String, password: String)

  case class Authenticate(username: String, password: String)

  case class AuthFailure(message: String)

  case object AuthSuccess

  object AuthManager {
    val AUTH_FAIL = "username not found"
    val AUTH_FAIL_PASS = "pass error"
    val AUTH_FAIL_SYSTEM = "system error"
  }

  class AuthManager extends Actor with ActorLogging {

    implicit val timeout: Timeout = Timeout(1 second)
    implicit val excutionContext: ExecutionContext = context.dispatcher

    protected val authOb = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authOb ! Write(username, password)
      case Authenticate(username, password) => handleAuthentication(username, password)


    }

    protected def handleAuthentication(username: String, password: String) = {
      val originalSender = sender()
      val future = authOb ? Read(username)

      /**
       * NEVER call methods on the actor instance OR access mutable state
       * in ONCOMPLETE
       * example:
       * -> sender()
       */
      future.onComplete {
        case Success(None) => originalSender ! AuthFailure(AUTH_FAIL)
        case Success(Some(dbPassword: String)) =>
          if (dbPassword == password) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAIL_PASS)
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAIL_SYSTEM)
      }
    }

  }

  class PipedAuthManager extends AuthManager {

    import AuthManager._

    override protected def handleAuthentication(username: String, password: String): Unit = {
      val future = authOb ? Read(username)

      val passwordFuture = future.mapTo[Option[String]]
      val responseFuture = passwordFuture.map {
        case None => AuthFailure(AUTH_FAIL)
        case Some(dbPassword: String) =>
          if (dbPassword == password) AuthSuccess
          else AuthFailure(AUTH_FAIL_PASS)
      }

      /*
      When the future completes, send the response to the actor ref in the arg list
       */
      responseFuture.pipeTo(sender())
    }
  }

}
