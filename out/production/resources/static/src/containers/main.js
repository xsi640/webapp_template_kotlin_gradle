import React, { Component } from 'react'
import ReactDOM from 'react-dom'
import * as menuData from '../store/menu'
import { Layout, Menu, Breadcrumb, Icon } from 'antd';
import { HashRouter as Router, Route, Link, Redirect } from 'react-router-dom'
const { SubMenu } = Menu;
const { Header, Content, Sider } = Layout;

export default class Main extends Component {

    constructor(props) {
        super(props);

        this.state = { nav: '1', submenu: '1', menu: '1' };

        this.handleClickNav = this.handleClickNav.bind(this);
        this.handleClickMenu = this.handleClickMenu.bind(this);
    }

    handleClickNav(item) {
        this.setState({ nav: item.key });
    }

    handleClickMenu(item) {
        let menu = menuData.dataMenus.find(i => item.key == i.id);
        //切换content
    }

    render() {
        let { dataNavs, dataSubmenus, dataMenus } = menuData;
        let navMenus = [];
        for (let dataNav of dataNavs) {
            navMenus.push(
                <Menu.Item key={dataNav.id} title={dataNav.desc}>
                    <Link to={"/" + dataNav.name}>{dataNav.name}</Link>
                </Menu.Item>)
        }
        let menus = [];
        let nav = dataNavs.find(item => item.id == this.state.nav);
        let currentDataSubmenus = dataSubmenus.filter(item => item.navId == this.state.nav);
        for (let dataSubmenu of currentDataSubmenus) {
            let menuItems = [];
            let dataMenuItems = dataMenus.filter(item => item.submenuId == dataSubmenu.id);
            for (let dataMenu of dataMenuItems) {
                menuItems.push(
                    <Menu.Item key={dataMenu.id}>
                        <Link to={"/" + nav.alias + "/" + dataMenu.alias}>{dataMenu.name}</Link>
                    </Menu.Item>)
            }
            menus.push(<SubMenu key={dataSubmenu.id} title={dataSubmenu.name}>{menuItems}</SubMenu>)
        }
        let routes = [];
        let defaultPath = '/';
        for (let dataMenu of dataMenus) {
            let nav = menuData.findNav(dataMenu.id);
            let path = "/" + nav.alias + "/" + dataMenu.alias;
            if (this.state.nav == nav.id && this.state.menu == dataMenu.id) {
                defaultPath = path;
            }
            routes.push(<Route key={dataMenu.id} path={path} component={dataMenu.component} />)
        }

        return <Router>
            <Layout>
                <Header className="header">
                    <div className="logo" />
                    <Menu
                        theme="dark"
                        mode="horizontal"
                        defaultSelectedKeys={[this.state.nav]}
                        style={{ lineHeight: '64px' }}
                        multiple={false}
                        onClick={this.handleClickNav}
                    >
                        {navMenus}
                    </Menu>
                </Header>
                <Layout>
                    <Sider width={200} style={{ background: '#fff' }}>
                        <Menu
                            mode="inline"
                            defaultSelectedKeys={[this.state.submenu]}
                            defaultOpenKeys={[this.state.submenu]}
                            multiple={false}
                            style={{ height: '100%', borderRight: 0 }}
                            onClick={this.handleClickMenu}
                        >
                            {menus}
                        </Menu>
                    </Sider>
                    <Layout style={{ padding: '0 24px 24px' }}>
                        <Content style={{ background: '#fff', padding: 24, margin: 0, minHeight: 280 }}>
                            {routes}
                            <Redirect from="/" exact to={defaultPath} />
                        </Content>
                    </Layout>
                </Layout>
            </Layout>
        </Router>;
    }
}