package com.suyang.domain

class ResponseMessage {
    var code = 0
    var message = ""
    var body: Any? = null

    companion object {

        fun ofSuccess(body: Any?): ResponseMessage {
            val result = ResponseMessage()
            result.body = body
            return result
        }
    }
}
