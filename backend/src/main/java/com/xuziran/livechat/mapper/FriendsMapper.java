package com.xuziran.livechat.mapper;

import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendsMapper {
    /** 发送（或重新发送）好友申请：from 请求 to */
    void addRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /** 是否存在待处理申请（from 请求 to） */
    int countPendingRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /** 同意申请：待处理 from → to 置为已同意（status=1） */
    int confirmRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /** 拒绝申请：待处理 from → to 置为已拒绝（status=2） */
    int rejectRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /** 建立好友关系（一对好友一行，user_id < friend_id；已存在则忽略） */
    void insertRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 是否为好友 */
    int countRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 删除好友关系 */
    void deleteRelation(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 作废两人之间的申请记录（删除好友时清理，避免旧申请重新冒泡） */
    void invalidateRequests(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /** 好友列表（含与该好友的私聊会话ID） */
    List<User> list(@Param("userId") Long userId,
                             @Param("offset") Integer offset,
                             @Param("size") Integer size);

    /** 好友总数 */
    Long countList(@Param("userId") Long userId);

    /** 收到的待处理申请列表 */
    List<User> listRequest(@Param("userId") Long userId);

    /** 是否已拉黑（双向） */
    int countBlock(@Param("userId") Long userId, @Param("targetId") Long targetId);

    /** 拉黑 */
    void insertBlock(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    /** 取消拉黑（双向删除），返回影响行数 */
    int deleteBlock(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    /** 拉黑列表（分页，被拉黑者信息） */
    List<UserVO> selectBlockedPage(@Param("userId") Long userId,
                                       @Param("offset") Integer offset,
                                       @Param("size") Integer size);

    /** 拉黑总数 */
    Long countBlocked(@Param("userId") Long userId);
}