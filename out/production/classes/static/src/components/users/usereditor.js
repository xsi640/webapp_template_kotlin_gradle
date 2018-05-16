import React, { Component } from 'react'
import { connect } from 'react-redux'
import * as UserAction from '../../actions/useraction'
import moment from 'moment'
import { Modal, Input, InputNumber, DatePicker, Radio, Alert, Button, Form } from 'antd'
const FormItem = Form.Item
const RadioGroup = Radio.Group
import PropTypes from 'prop-types'
import './usereditor.scss'
import { checkUserName } from '../../consts/api';

class UserEditor extends Component {

    _user;

    constructor(props) {
        super(props);

        this.state = {
            title: '',
            error: '',
            visible: false,
            loading: false,
            isModify: false,
        };

        this.handleOk = this.handleOk.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleSaveUser = this.handleSaveUser.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        console.log('nextProps', nextProps)
        if (typeof nextProps.user !== 'undefined') {
            this.props.onClose(nextProps.user);
            this.setState({
                visible: false,
                loading: false
            });
        }
        if (nextProps.error !== '') {
            this.setState({
                loading: false,
                error: nextProps.error
            });
        }
        if (typeof nextProps.checkName !== 'undefined') {
            this.setState({
                loading: false
            });
            if (nextProps.checkName === true) {
                this.props.form.setFields({
                    loginName: {
                        errors: [new Error('用户名重复。')]
                    }
                })
            } else {
                this.handleSaveUser();
            }
        } else if (nextProps.error) {
            this.setState({ loading: false });
            message.error(nextProps.error);
        }
        this.props.clear();
    }

    show(user) {
        this.props.form.resetFields();
        this._user = user;
        if (typeof this._user === 'undefined') {
            this.setState({
                isModify: false,
                title: '新增用户',
                error: '',
                visible: true,
                loading: false,
                checkNameStatus: undefined,
            });
        } else {
            this.setState({
                isModify: true,
                title: '修改用户',
                error: '',
                visible: true,
                loading: false,
                checkNameStatus: undefined,
            });
        }
    }

    handleOk(e) {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                if (typeof this._user === 'undefined') {
                    this.values = values;
                    this.props.checkLoginName(values.loginName);
                } else {
                    values = { id: this._user.id, ...values, birthday: moment(values.birthday).format('YYYY-MM-DD HH:mm:ss') };
                    this.props.modify(values);
                }
            }
        });
    }

    handleSaveUser() {
        this.setState({ loading: true })
        if (typeof this._user === 'undefined') {
            this.props.save({ ...this.values, birthday: moment(this.values.birthday).format('YYYY-MM-DD HH:mm:ss') })
        }
    }

    handleCancel(e) {
        e.preventDefault();
        this.setState({ visible: false });
        this.props.onClose();
    }

    render() {
        const { title, error, visible, loading, isModify, checkNameStatus } = this.state;
        const { getFieldDecorator } = this.props.form;
        const formItemLayout = {
            labelCol: { span: 4 },
            wrapperCol: { span: 20 },
        };
        let pwdRules = isModify ? [] : [{
            required: true,
            message: '请输入登录密码。',
            whitespace: true,
        }, {
            min: 6,
            max: 32,
            message: '密码长度6~12个字符'
        }];
        return (
            <div>
                <Modal title={title} visible={visible} closable={false}
                    footer={[
                        <Button key="back" size="large" onClick={this.handleCancel}>取消</Button>,
                        <Button key="submit" type="primary" htmlType="submit" size="large" onClick={this.handleOk} loading={loading}>确定</Button>
                    ]} >
                    <Form>
                        <div className='alert'>
                            {
                                typeof error === 'string' && error !== '' ?
                                    <Alert message={error} type="error" closable={true} style={{ marginBottom: '20px' }} />
                                    : null
                            }
                        </div>
                        <div className='user'>
                            <FormItem label="登录名" {...formItemLayout}>
                                {
                                    getFieldDecorator('loginName', {
                                        rules: [{
                                            required: true,
                                            message: '请输入登录名',
                                            whitespace: true,
                                        }, {
                                            min: 3,
                                            max: 16,
                                            message: '用户名称为3~16个字符'
                                        }],
                                        initialValue: this._user ? this._user.loginName : '',
                                    })(
                                        <Input placeholder="请输入登录名" disabled={isModify} />
                                    )
                                }
                            </FormItem>
                            <FormItem label="登录密码" {...formItemLayout}>
                                {
                                    getFieldDecorator('loginPwd', {
                                        rules: pwdRules
                                    })(
                                        <Input placeholder="请输入登录密码" type="password" />
                                    )
                                }
                            </FormItem>
                            <FormItem label="重复密码" {...formItemLayout}>
                                {
                                    getFieldDecorator('loginRePwd', {
                                        rules: [...pwdRules, {
                                            validator: (rule, value, callback) => {
                                                const form = this.props.form;
                                                if (value && value !== form.getFieldValue('loginPwd')) {
                                                    callback('两次密码输入不同！');
                                                } else {
                                                    callback();
                                                }
                                            }
                                        }]
                                    })(
                                        <Input placeholder="请重新输入登录密码" type="password" />
                                    )
                                }
                            </FormItem>
                            <FormItem label="真实姓名" {...formItemLayout}>
                                {
                                    getFieldDecorator('realName', {
                                        rules: [{
                                            required: true,
                                            message: '请输入真实姓名。'
                                        }],
                                        initialValue: this._user ? this._user.realName : ''
                                    })(
                                        <Input placeholder="请输入真实姓名" />
                                    )
                                }
                            </FormItem>
                            <FormItem label="性别" {...formItemLayout}>
                                {
                                    getFieldDecorator('sex', {
                                        rules: [{
                                            type: 'number'
                                        }],
                                        initialValue: this._user ? this._user.sex : 0
                                    })(
                                        <RadioGroup name="sex">
                                            <Radio value={1}>男</Radio>
                                            <Radio value={2}>女</Radio>
                                            <Radio value={0}>保密</Radio>
                                        </RadioGroup>
                                    )
                                }
                            </FormItem>
                            <FormItem label="生日" {...formItemLayout}>
                                {
                                    getFieldDecorator('birthday', {
                                        initialValue: this._user ? moment(this._user.birthday) : moment().add(-18, 'y')
                                    })(
                                        <DatePicker format="YYYY-MM-DD" allowClear={false} />
                                    )
                                }
                            </FormItem>
                            <FormItem label="地址" {...formItemLayout}>
                                {
                                    getFieldDecorator('address', {
                                        rules: [{}],
                                        initialValue: this._user ? this._user.address : ''
                                    })(
                                        <Input placeholder="请输入地址" />
                                    )
                                }
                            </FormItem>
                        </div>
                    </Form>
                </Modal>
            </div>
        )
    }
}

UserEditor.propTypes = {
    onClose: PropTypes.func,
}

const mapStateToProps = (state) => {
    return state.UserReducer;
}

const WrappedUserEditor = Form.create({ wrappedComponentRef: true })(UserEditor);
export default connect(mapStateToProps, UserAction, null, { withRef: true })(WrappedUserEditor)