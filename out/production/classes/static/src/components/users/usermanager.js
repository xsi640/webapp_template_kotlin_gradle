import React, { Component } from 'react'
import { connect } from 'react-redux'
import { withRouter } from 'react-router-dom'
import { Button, Table, Pagination, message } from 'antd'
import UserEditor from './usereditor'
import * as UserAction from '../../actions/useraction'

class UserManager extends Component {

    constructor(props) {
        super(props);

        this.state = {
            loading: false,
            data: [],
            selectedRowKeys: [],
            total: 0,
            pageIndex: 1,
            pageSize: 10
        };

        this.handleSelectedChange = this.handleSelectedChange.bind(this);
        this.show = this.show.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleModify = this.handleModify.bind(this);
        this.handleDelete = this.handleDelete.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.handlePageSizeChange = this.handlePageSizeChange.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.users) {
            this.setState({
                loading: false,
                data: nextProps.users.content,
                total: nextProps.users.totalElements,
                pageIndex: nextProps.users.number + 1,
                pageSize: nextProps.users.size,
            })
        } else if (nextProps.deleteUserId) {
            let index = this.state.data.findIndex(item => item.id === nextProps.deleteUserId);
            if (index >= 0) {
                this.state.data.splice(index, 1);
            }
            let selected = this.state.selectedRowKeys;
            if (selected.length > 0) {
                if (nextProps.deleteUserId == selected[0]) {
                    selected.splice(0, 1);
                }
                if (selected.length > 0) {
                    this.props.del(selected[0]);
                } else {
                    this.setState({ loading: false });
                }
            }
        } else if (nextProps.error) {
            this.setState({ loading: false });
            message.error(nextProps.error);
        }
    }

    handleSelectedChange(selectedRowKeys) {
        this.setState({ selectedRowKeys: selectedRowKeys });
    }

    handleModify() {
        let id = this.state.selectedRowKeys[0];
        let user;
        for (let u of this.state.data) {
            if (u.id === id) {
                user = u;
                break;
            }
        }
        if (user) {
            this.show(user);
        }
    }

    handleDelete() {
        this.props.del(this.state.selectedRowKeys[0])
        this.setState({ loading: true });
    }

    show(user) {
        this.userEditor.show(user);
    }

    componentWillMount() {
        this.props.list(this.state.pageIndex, this.state.pageSize);
        this.setState({ loading: true });
    }

    handleClose(user) {
        console.log('handleClose', user);
        if (user) {
            //this.props.list();
            let u = this.state.data.find(item => item.id === user.id);
            if (u) {
                for (let key in u) {
                    u[key] = user[key];
                }
            } else {
                //this.state.data.push(user);
                this.props.list(this.state.pageIndex, this.state.pageSize);
            }
        }
    }

    handlePageChange(page, pageSize) {
        this.props.list(page, pageSize);
    }

    handlePageSizeChange(current, size) {
        this.props.list(1, size);
    }

    render() {
        const columns = [{
            title: '登录名称',
            dataIndex: 'loginName',
        }, {
            title: '真实姓名',
            dataIndex: 'realName',
        }, {
            title: '性别',
            dataIndex: 'sex',
            render: (value) => {
                switch (value) {
                    case 0:
                        return '保密';
                    case 1:
                        return '男';
                    case 2:
                        return '女';
                }
            }
        }, {
            title: '生日',
            dataIndex: 'birthday',
        }, {
            title: '地址',
            dataIndex: 'address'
        }];

        const { selectedRowKeys, data, loading, modalError, modalUser, modalLoading, modalVisible, total, pageIndex, pageSize } = this.state;
        const rowSelection = {
            selectedRowKeys,
            onChange: this.handleSelectedChange
        };
        const hasSelected = selectedRowKeys.length > 0 && !loading;
        const isSingleSelected = selectedRowKeys.length === 1 && !loading;

        return (
            <div>
                <div>
                    <Button type="primary" style={{ marginRight: '15px' }} onClick={() => this.show(undefined)}>新增</Button>
                    <Button type="primary" disabled={!isSingleSelected} style={{ marginRight: '15px' }} onClick={this.handleModify}>修改</Button>
                    <Button type="primary" disabled={!hasSelected} style={{ marginRight: '15px' }} onClick={this.handleDelete}>删除</Button>
                </div>
                <UserEditor wrappedComponentRef={(ref) => this.userEditor = ref} onClose={this.handleClose} />
                <Table rowKey="id" rowSelection={rowSelection} columns={columns} dataSource={data} loading={loading} style={{ marginTop: '10px' }} />
                <Pagination total={total} pageSize={pageSize} current={pageIndex} onChange={this.handlePageChange} onShowSizeChange={this.handlePageSizeChange} showSizeChanger={true} style={{ marginTop: '15px' }} />
            </div>
        );
    }
}

const mapStateToProps = (state) => {
    return state.UserReducer;
}

export default connect(mapStateToProps, UserAction)(UserManager)