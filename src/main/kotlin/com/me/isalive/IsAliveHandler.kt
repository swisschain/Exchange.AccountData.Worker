package com.me.isalive

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.OK
import fi.iki.elonen.router.RouterNanoHTTPD

class IsAliveHandler: RouterNanoHTTPD.DefaultHandler() {
    override fun getStatus(): NanoHTTPD.Response.IStatus {
       return OK
    }

    override fun getMimeType(): String {
        return NanoHTTPD.MIME_PLAINTEXT
    }

    override fun getText(): String {
        return "Name=Exchange.AccountData.Worker"
    }
}