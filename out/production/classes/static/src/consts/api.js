/**
 * 存储服务器API接口地址
 */
const baseurl = "/api"

export const getUsers = baseurl + '/user'
export const saveUser = baseurl + '/user'
export const deleteUser = baseurl + '/user/{id}'
export const checkUserName = baseurl + '/user/checkname'