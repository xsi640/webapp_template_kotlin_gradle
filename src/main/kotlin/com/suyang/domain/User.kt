package com.suyang.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*


@Entity
@Table(indexes = arrayOf(Index(name = "login_name_index", columnList = "loginName", unique = true)))
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    @Column(nullable = false)
    lateinit var loginName: String
    @Column(nullable = false)
    @JsonIgnore
    lateinit var loginPwd: String
    @Column(nullable = false)
    @JsonIgnore
    lateinit var loginSalt: String
    @Column(nullable = false)
    lateinit var lastLoginIP: String
    @Column(nullable = false)
    lateinit var lastLoginTime: Date
    @Column(nullable = false)
    var loginCount: Long = 0
    @Column(nullable = false)
    lateinit var realName: String
    var sex: Int = 0
    lateinit var birthday: Date
    lateinit var address: String

}
