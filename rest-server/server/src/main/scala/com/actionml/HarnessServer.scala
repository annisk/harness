package com.actionml

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.actionml.admin.{Administrator, MongoAdministrator}
import com.actionml.router.config.AppConfig
import com.actionml.router.http.RestServer
import com.actionml.router.http.routes._
import com.actionml.router.service._
import com.actionml.authserver.router.AuthServerProxyRouter
import com.actionml.authserver.service.AuthorizationService
import com.actionml.authserver.services.{AuthServerProxyService, AuthServerProxyServiceImpl, CachedAuthorizationService, ClientAuthorizationService}
import com.actionml.core.store.backends.MongoStorage
import com.typesafe.scalalogging.LazyLogging
import scaldi.Module
import scaldi.akka.AkkaInjectable

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  *
  * @author The ActionML Team (<a href="http://actionml.com">http://actionml.com</a>)
  * 28.01.17 11:54
  */
object HarnessServer extends App with AkkaInjectable with LazyLogging {

  implicit val injector = new BaseModule

  inject[RestServer].run()

  sys.addShutdownHook {
    logger.info("Shutting down Harness Server")
    MongoStorage.close
  }
}

class BaseModule extends Module with LazyLogging {

  val config = AppConfig.apply
  bind[AppConfig] to config

  bind[ActorSystem] to ActorSystem(inject[AppConfig].actorSystem.name) destroyWith(terminateActorSystem)
  private def terminateActorSystem(system: ActorSystem): Unit = {
    logger.info("Terminating actor system in the Harness Server...")
    system.whenTerminated.foreach { t =>
      logger.info(s"Actor system terminated: $t")
    }
    system.terminate()
  }

  implicit lazy val system: ActorSystem = inject [ActorSystem]
  bind[ExecutionContext] to system.dispatcher
  bind[ActorMaterializer] to ActorMaterializer()

  bind[RestServer] to new RestServer

  bind[CheckRouter] to new CheckRouter
  bind[EventsRouter] to new EventsRouter
  bind[EnginesRouter] to new EnginesRouter
  bind[QueriesRouter] to new QueriesRouter
  bind[CommandsRouter] to new CommandsRouter
  bind[AuthServerProxyRouter] to new AuthServerProxyRouter(config)

  bind[EventService] to new EventServiceImpl
  bind[EngineService] to new EngineServiceImpl
  bind[QueryService] to new QueryServiceImpl

  bind[AuthServerProxyService] to new AuthServerProxyServiceImpl
  bind[AuthorizationService] to new CachedAuthorizationService

  binding identifiedBy 'EventService to AkkaInjectable.injectActorRef[EventService]("EventService")
  binding identifiedBy 'QueryService to AkkaInjectable.injectActorRef[QueryService]("QueryService")
  binding identifiedBy 'EngineService to AkkaInjectable.injectActorRef[EngineService]("EngineService")

  bind[Administrator] to new MongoAdministrator initWith(_.init())

}
