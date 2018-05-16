package com.suyang.exceptions

class APIException(type: APIExceptionType) : Exception() {

    var type: APIExceptionType = type
}
