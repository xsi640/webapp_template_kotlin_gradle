import * as MESSAGE from '../consts/message'

const initialStatus = {};

export const UserReducer = (state = initialStatus, action) => {
    switch (action.type) {
        case MESSAGE.USER_LIST:
            return {
                ...initialStatus,
                users: action.payload,
                error: action.error
            }
        case MESSAGE.USER_SAVE:
        case MESSAGE.USER_MODIFY:
            return {
                ...initialStatus,
                user: action.payload,
                error: action.error
            }
        case MESSAGE.USER_DELETE:
            return {
                ...initialStatus,
                deleteUserId: action.payload,
                error: action.error
            }
        case MESSAGE.USER_CHECK_NAME:
            return {
                ...initialStatus,
                checkName: action.payload,
                error: action.error
            };
        case MESSAGE.CLEAR:
            return {};
        default:
            return state;
    }
}
