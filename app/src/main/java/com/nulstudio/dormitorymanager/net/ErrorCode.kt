package com.nulstudio.dormitorymanager.net

object ErrorCode {
    const val NDM_ERRS_OK = 0
    const val NDM_ERR_OK = 0

    const val NDM_ERRS_SYSTEM = 1
    const val NDM_ERR_SCRIPT_ERROR = 10001
    const val NDM_ERR_SQL_ERROR = 10002

    const val NDM_ERRS_ACCOUNT = 2
    const val NDM_ERR_USER_NOT_EXIST = 20001
    const val NDM_ERR_WRONG_PASSWORD = 20002
    const val NDM_ERR_USER_IS_BANNED = 20003

    const val NDM_ERR_INVALID_INVITE_CODE = 20101
    const val NDM_ERR_INVITE_CODE_USED_UP = 20102
    const val NDM_ERR_INVITE_CODE_BLOCKED = 20103
    const val NDM_ERR_USER_ALREADY_EXIST = 20104

    const val NDM_ERRS_DORM_MGR = 3

    const val NDM_ERR_DORM_ALREADY_EXIST = 30001
    const val NDM_ERR_DORM_CODE_NOT_EXIST = 30002

}