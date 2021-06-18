package com.ffst.dustbinbrain.kotlin_mvp.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.ffst.dustbinbrain.kotlin_mvp.app.KotlinMvpApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.*

/**
 * Created by LiuJW
 *on 2021/6/16
 */
class DataBaseUtil {
    companion object {
        private var helper: DaoMaster.DevOpenHelper? = null
        private var database: SQLiteDatabase? = null
        private var daoMaster: DaoMaster? = null
        private var mDaoSession: DaoSession? = null

        @Volatile
        private var instance: DataBaseUtil? = null


        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                //  创建数据库database.db
                helper = DaoMaster.DevOpenHelper(context, "database.db")
                //  获取可写数据库
                //  获取可写数据库
                database = helper!!.writableDatabase
                //  获取数据库对象
                //  获取数据库对象
                daoMaster = DaoMaster(database)
                //  获取Dao对象管理者
                //  获取Dao对象管理者
                mDaoSession = daoMaster!!.newSession()
                instance ?: DataBaseUtil().also { instance = it }
            }
    }

    fun getDaoSession() = mDaoSession

    /**
     * 设置垃圾箱配置
     * @param list 垃圾箱配置
     * */
    fun setDustBinStateConfig(list:MutableList<DustbinStateBean>){
        val dustbinBeanDao: DustbinStateBeanDao = getDaoSession()!!.dustbinStateBeanDao

        //  删除之前所有配置信息
        dustbinBeanDao.deleteAll()

        //  插入新的配置
        dustbinBeanDao.insertOrReplaceInTx(list)
    }

    /**
     * 插入一条绑定用户id和facetoken的记录
     */
    @Synchronized
    fun insertUserIdAndFaceToken(userId: Long, faceToken: String?) {

        //  查询数据库中是否有登陆记录
        var userMessage: UserMessage = getDaoSession()!!.userMessageDao.queryBuilder()
            .where(UserMessageDao.Properties.UserId.eq(userId)).build().unique()
        if (userMessage != null) {
            //  说明存在该记录，则使用次数添加 + 1，这一步骤主要是为了以后清理使用次数较少的人脸

            //  使用次数 + 1
            userMessage.setUsedNumber(userMessage.getUsedNumber() + 1)
            //  上次使用时间
            userMessage.setLastUsedTime(System.currentTimeMillis())
            //  插入 或者 替换 到数据库
            getDaoSession()!!.userMessageDao.insertOrReplace(userMessage)
        } else {
            // 在这台安卓机上用户还没有登陆过，则添加一条新的记录
            userMessage = UserMessage()
            //  对应底库中的faceToken
            userMessage.setFaceToken(faceToken)
            //  设置用户类型
            userMessage.setUserType(KotlinMvpApp.userType!!.toLong())
            //  上次使用时间
            userMessage.setLastUsedTime(System.currentTimeMillis())
            //  在数据库中的注册时间
            userMessage.setRegisterTime(System.currentTimeMillis())
            //  使用该次数
            userMessage.setUsedNumber(1)
            //  设置服务器传过来的用户id
            userMessage.setUserId(userId)
            //  插入到数据库

            //  人脸匹配但不通过 修改 insert 为 insertOrReplace
            getDaoSession()!!.userMessageDao.insertOrReplace(userMessage)
        }
    }

    /**
     * 插入一条绑定用户id和facetoken的记录
     */
    @Synchronized
    fun insertUserIdAndFaceTokenThread(userId: Long, userType: Long, faceToken: String?) {

        //  查询数据库中是否有登陆记录
        var userMessage = getDaoSession()!!.userMessageDao.queryBuilder()
            .where(UserMessageDao.Properties.UserId.eq(userId)).build().unique()
        if (userMessage != null) {
            //  说明存在该记录，则使用次数添加 + 1，这一步骤主要是为了以后清理使用次数较少的人脸

            //  使用次数 + 1
            userMessage.usedNumber = userMessage.usedNumber + 1
            //  上次使用时间
            userMessage.lastUsedTime = System.currentTimeMillis()
            //  插入 或者 替换 到数据库
            getDaoSession()!!.userMessageDao.insertOrReplace(userMessage)
        } else {
            // 在这台安卓机上用户还没有登陆过，则添加一条新的记录
            userMessage = UserMessage()
            //  对应底库中的faceToken
            userMessage.faceToken = faceToken
            //  设置用户类型
            userMessage.userType = userType
            //  上次使用时间
            userMessage.lastUsedTime = System.currentTimeMillis()
            //  在数据库中的注册时间
            userMessage.registerTime = System.currentTimeMillis()
            //  使用该次数
            userMessage.usedNumber = 1
            //  设置服务器传过来的用户id
            userMessage.userId = userId
            //  插入到数据库

            //  人脸匹配但不通过 修改 insert 为 insertOrReplace
            getDaoSession()!!.userMessageDao.insertOrReplace(userMessage)
        }
    }

}