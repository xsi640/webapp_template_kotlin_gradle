package com.suyang

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.suyang.domain.ResponseMessage
import com.suyang.exceptions.APIException
import com.suyang.exceptions.APIExceptionType
import com.suyang.exceptions.ErrorType
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import java.io.IOException
import java.sql.SQLException
import java.util.HashMap
import javax.servlet.http.HttpServletRequest

/**
 * 错误处理类
 * @author SuYang
 */
@ControllerAdvice
class GlobalDefaultExceptionHandler {

    @ResponseBody
    @ExceptionHandler(Exception::class)
    fun defaultErrorHandler(request: HttpServletRequest, e: Exception): ResponseMessage {
        val key = APIExceptionType.UnKnow.name
        return createErrorMessage(request, key, ErrorType.System.value)
    }

    @ResponseBody
    @ExceptionHandler(SQLException::class)
    fun sqlErrorHandler(request: HttpServletRequest, e: Exception): ResponseMessage {
        val key = APIExceptionType.UnKnow.name
        return createErrorMessage(request, key, ErrorType.Database.value)
    }

    @ResponseBody
    @ExceptionHandler(APIException::class)
    fun handleAPIException(request: HttpServletRequest, ex: APIException): ResponseMessage {
        return createErrorMessage(request, ex.type.name, ErrorType.API.value)
    }

    private fun createErrorMessage(request: HttpServletRequest, key: String, code: Int): ResponseMessage {
        val result = ResponseMessage()
        result.code = code

        var msg = ""
        var local = request.getHeader("local")
        if (StringUtils.isEmpty(local)) {
            local = DEFAULT_LOCAL
        }
        val map = errorMessage[key]
        if (map != null) {
            msg = map[local]!!
            if (StringUtils.isEmpty(msg)) {
                msg = map[DEFAULT_LOCAL]!!
            }
        }
        result.message = msg
        return result
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalDefaultExceptionHandler::class.java)

        /**
         * 错误信息缓存
         * key=错误信息key
         * value={key=语言key, value=错误信息}
         */
        private val errorMessage = HashMap<String, HashMap<String, String>>()
        /**
         * 存储错误信息的文件
         */
        private val ERROR_FILE = "errormessage.yml"
        /**
         * 默认语言
         */
        private val DEFAULT_LOCAL = "cn"

        init {
            try {
                initialize()
            } catch (e: JsonProcessingException) {
                logger.error(e.message, e)
            } catch (e: IOException) {
                logger.error(e.message, e)
            }

        }

        /**
         * 初始化
         * 将错误信息文件的内容读取到缓存
         * @throws JsonProcessingException
         * @throws IOException
         */
        @Synchronized
        @Throws(JsonProcessingException::class, IOException::class)
        private fun initialize() {
            val mapper = ObjectMapper(YAMLFactory())
            val root = mapper.readTree(ClassLoader.getSystemResourceAsStream(ERROR_FILE))
            val elements = root.fields()
            while (elements.hasNext()) {
                val entry = elements.next()
                val key = entry.key
                val map = HashMap<String, String>()
                val children = entry.value
                val els = children.fields()
                while (els.hasNext()) {
                    val v = els.next()
                    map[v.key] = v.value.asText()
                }
                errorMessage[key] = map
            }
        }
    }

}
