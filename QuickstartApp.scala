package com.app {
    import akka.actor.typed.ActorSystem
    import akka.actor.typed.scaladsl.Behaviors
    import akka.http.scaladsl.Http
    import akka.http.scaladsl.server.Route
    import scala.util.Failure
    import scala.util.Success

    object QuickstartApp {
        private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
            // Akka HTTP still needs a classic ActorSystem to start
            import system.executionContext
        
            val futureBinding = Http().newServerAt("localhost", 1112).bind(routes)
            futureBinding.onComplete {
                case Success(binding) =>
                    val address = binding.localAddress
                    system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
                case Failure(ex) =>
                    system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
                    system.terminate()
            }
        }

        def main(args: Array[String]): Unit = {
            val rootBehavior = Behaviors.setup[Nothing] { context =>
                val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
                context.watch(userRegistryActor)

                val routes = new UserRoutes(userRegistryActor)(context.system)
                startHttpServer(routes.userRoutes)(context.system)

                Behaviors.empty
            }
            val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
        }
    }
}

