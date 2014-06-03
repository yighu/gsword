class BootStrap {
	def eventBus
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
eventBus.onEvent('ghost', { msg -> 
println "Message incoming: $msg"
eventBus.sendMessage('ghost', [message: 'echo msg $msg'])
})
     }
     def destroy = {
     }
} 
