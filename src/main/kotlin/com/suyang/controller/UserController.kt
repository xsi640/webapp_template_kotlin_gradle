package com.suyang.controller

import com.suyang.domain.User
import com.suyang.exceptions.APIException
import com.suyang.exceptions.APIExceptionType
import com.suyang.exceptions.ErrorType
import com.suyang.repository.UserRepository
import com.suyang.utils.CryptoUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class UserController {

    @Autowired
    private
    lateinit var userRepository: UserRepository

    @RequestMapping(value = ["/api/user/{id}"], method = [RequestMethod.GET])
    fun findOne(@PathVariable("id") id: Int): User? {
        return this.userRepository.findById(id).orElse(null)
    }

    @RequestMapping(value = ["/api/user"], method = [RequestMethod.GET])
    fun findAll(@RequestParam(name = "pageIndex", required = false, defaultValue = "1") pageIndex: Int,
                @RequestParam(name = "pageSize", required = false, defaultValue = "10") pageSize: Int): Page<User> {
        val pageable = PageRequest.of(pageIndex - 1, pageSize)
        return this.userRepository.findAll(pageable)
    }

    @RequestMapping(value = ["/api/user"], method = [RequestMethod.POST])
    fun create(loginName: String,
               loginPwd: String,
               realName: String,
               sex: Int, birthday: Date,
               address: String): User {
        var user = User()
        user.loginName = loginName
        user.realName = realName
        user.lastLoginIP = ""
        user.lastLoginTime = Date()
        user.loginCount = 0
        user.sex = sex
        user.birthday = birthday
        user.address = address
        var salt = CryptoUtils.salt
        user.loginSalt = salt
        user.loginPwd = CryptoUtils.getHash(loginPwd, salt)
        return this.userRepository.save(user)
    }

    @RequestMapping(value = ["/api/user"], method = [RequestMethod.PUT])
    fun modify(id: Int,
               loginPwd: String,
               realName: String,
               sex: Int, birthday: Date, address: String): User? {
        var user: User = this.userRepository.findById(id).orElse(null) ?: return null

        user.realName = realName
        user.sex = sex
        user.birthday = birthday
        user.address = address
        if (!StringUtils.isEmpty(loginPwd)) {
            val salt = CryptoUtils.salt
            user.loginSalt = salt
            user.loginPwd = CryptoUtils.getHash(loginPwd, salt)
        }
        return this.userRepository.save(user)
    }

    @RequestMapping(value = ["/api/user/{id}"], method = [RequestMethod.DELETE])
    @Throws(Exception::class)
    fun delete(@PathVariable("id") id: Int): Int {
        var result = 0
        val user = this.userRepository.findById(id).orElse(null)
        if (user != null) {
            if (user.loginName == "admin") {
                throw APIException(APIExceptionType.NoLimit)
            }
            this.userRepository.delete(user)
            result = 1
        }
        return result
    }

    @RequestMapping(value = ["/api/user/checkname"])
    fun existsName(loginName: String): Boolean {
        return this.userRepository.countByLoginName(loginName) > 0
    }
}
