package com.suyang

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import java.text.SimpleDateFormat
import java.util.*

@SpringBootApplication
class Application {

    /**
     * 默认String to data类型的转换
     * @return
     */
    @Bean
    fun addNewConvert(): Converter<String, Date> {
        return Converter {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            sdf.parse(it)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}
