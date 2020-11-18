package com.me.isalive

import fi.iki.elonen.router.RouterNanoHTTPD

class IsAliveService(port: Int): RouterNanoHTTPD(port) {

    init {
        addMappings()
        start(SOCKET_READ_TIMEOUT, false)
    }

    override fun addMappings() {
        addRoute("/api/isalive", IsAliveHandler::class.java)
    }
}