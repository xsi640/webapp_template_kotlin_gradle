/**
 * 调用api接口的方法，类似js版本sdk
 */

import { Request, Method } from './webapi';
import * as API from '../consts/api';

/**
 * 获取所有用户
 * @param {number} pageIndex 
 * @param {number} pageSize 
 * @param {func} resolve 
 * @param {func} reject 
 */
export const getUsers = (pageIndex, pageSize, resolve, reject) => {
    Request(API.getUsers, Method.GET, {
        pageIndex,
        pageSize
    }, resolve, reject);
}

/**
 * 保存用户
 * @param {object} user 
 * @param {func} resolve 
 * @param {func} reject 
 */
export const saveUser = (user, resolve, reject) => {
    return Request(API.saveUser, Method.POST, {
        loginName: user.loginName,
        realName: user.realName,
        loginPwd: user.loginPwd,
        sex: user.sex,
        birthday: user.birthday,
        address: user.address,
    }, resolve, reject);
}

/**
 * 修改用户
 * @param {object} user 
 * @param {func} resolve 
 * @param {func} reject 
 */
export const modifyUser = (user, resolve, reject) => {
    return Request(API.saveUser, Method.PUT, {
        id: user.id,
        realName: user.realName,
        loginPwd: user.loginPwd,
        sex: user.sex,
        birthday: user.birthday,
        address: user.address,
    }, resolve, reject)
}

/**
 * 删除用户
 * @param {string} id 
 * @param {func} resolve 
 * @param {func} reject 
 */
export const deleteUser = (id, resolve, reject) => {
    let url = API.deleteUser.replace(/{id}/g, id);
    return Request(url, Method.DELETE, {}, resolve, reject)
}

/**
 * 检查用户名称是否重复
 * @param {string} loginName 
 * @param {func} resolve 
 * @param {func} reject 
 */
export const checkUser = (loginName, resolve, reject) => {
    let url = API.checkUserName;
    return Request(url, Method.GET, { loginName: loginName }, resolve, reject)
}