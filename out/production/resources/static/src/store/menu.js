/**
 * 存储菜单的常量
 */
import UserManager from '../components/users/usermanager'

export const dataNavs = [{ id: 1, name: '资源管理', alias: 'resouces' },
{ id: 2, name: 'nav2', alias: 'nav2...' }];
export const dataSubmenus = [{ id: 1, navId: 1, name: '用户管理' },
{ id: 2, navId: 1, name: 'submenu2' }]
export const dataMenus = [{ id: 1, submenuId: 1, name: '用户管理', alias: 'user', component: UserManager },
{ id: 2, submenuId: 1, name: 'menu2', alias: 'menu2', component: undefined },
{ id: 3, submenuId: 2, name: 'menu3', alias: 'menu3', component: undefined },
{ id: 4, submenuId: 2, name: 'menu4', alias: 'menu4', component: undefined },]

export const findNav = (menuId) => {
    let nav;
    let dataMenu = dataMenus.find(item => item.id === menuId);
    if (typeof dataMenu === 'undefined')
        return nav;
    let dataSubmenu = dataSubmenus.find(item => item.id == dataMenu.submenuId);
    if (typeof dataSubmenu === 'undefined')
        return nav;
    nav = dataNavs.find(item => item.id === dataSubmenu.navId);
    return nav;
}