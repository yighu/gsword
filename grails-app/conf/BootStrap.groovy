class BootStrap {
     def init = { servletContext ->
/*
 def vertx = Vertx.newVertx()
def httpServer = vertx.createHttpServer()
vertx.createSockJSServer(httpServer).installApp(prefix: '/book') { sock ->
sock.dataHandler { buff ->
sock << buff
}
}
 
httpServer.listen(8585)
*/
     }
     def destroy = {
     }
} 
