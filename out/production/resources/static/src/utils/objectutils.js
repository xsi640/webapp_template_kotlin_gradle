/**
 * js对象公共辅助
 */

/**
 * 获取对象的属性
 * @param {object} 要获取属性的对象
 * @param {string} 属性名称
 * @returns 属性的值
 */
export const getProp = (obj, prop) => {
    if (typeof obj === 'undefined' || obj == null || typeof prop !== 'string' || prop === '')
        return '';

    if (typeof obj[prop] === 'undefined') {
        return '';
    } else {
        return obj[prop];
    }
}

/**
 * 将对象的所有属性等于undefined的都赋值成空字符串
 * @param {object} 修复的对象
 */
export const fixObject = (obj) => {
    if (typeof obj === 'undefined') {
        return;
    }

    Object.keys(obj).forEach(item => {
        if (typeof obj[item] === 'undefined') {
            obj[item] = '';
        }
    })
}