package com.xuziran.livechat.mapper;

import com.xuziran.livechat.model.entity.ChatMessage;
import com.xuziran.livechat.model.entity.Conversation;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.ConversationVO;
import com.xuziran.livechat.model.vo.MessageVO;
import com.xuziran.livechat.model.vo.UnreadStat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessagesMapper {
    /** 按发送者查询用户（WebSocket 回显用） */
    User queryUserById(Long id);

    // ---- 会话 ----

    /** 新建会话，回填 id */
    void insertConversation(Conversation conversation);

    Conversation selectConversationById(Long id);

    /** 查找两个用户之间已存在的私聊会话 */
    Long selectP2PConversationId(@Param("uidA") Long uidA, @Param("uidB") Long uidB);

    /** 加入会话成员（重复则忽略） */
    void insertMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /** 会话成员校验：返回该用户在该会话中的成员记录数（0=非成员） */
    Long selectConversationMember(@Param("conversationId") Long conversationId,
                                  @Param("userId") Long userId);

    /** 我的会话列表（群聊 + 已建立的私聊，含对方信息、最后一条消息、未读数） */
    List<ConversationVO> selectMyConversations(@Param("userId") Long userId,
                                                        @Param("offset") Integer offset,
                                                        @Param("size") Integer size);

    /** 我的会话总数 */
    Long countMyConversations(@Param("userId") Long userId);

    /** 会话成员列表（含在线状态，分页） */
    List<User> selectConversationMembers(@Param("conversationId") Long conversationId,
                                                                  @Param("offset") Integer offset,
                                                                  @Param("size") Integer size);

    /** 会话全部成员（广播用，无分页） */
    List<User> selectAllConversationMembers(@Param("conversationId") Long conversationId);

    /** 会话成员总数 */
    Long countConversationMembers(@Param("conversationId") Long conversationId);

    /** 批量加入成员（重复则忽略），userIds 不可为空 */
    void insertMembers(@Param("conversationId") Long conversationId,
                       @Param("userIds") List<Long> userIds);

    /** 移除成员 */
    void deleteMember(@Param("conversationId") Long conversationId,
                      @Param("userId") Long userId);

    // ---- 消息 ----

    void insertMessage(ChatMessage chatMessage);

    /** 游标分页：取某会话中 id < beforeId 的最新 limit 条（送前端前应用层倒序） */
    List<MessageVO> selectHistoryPage(@Param("conversationId") Long conversationId,
                                      @Param("beforeId") Long beforeId,
                                      @Param("limit") Integer limit);

    /** 会话当前最大消息 id（标记已读游标用） */
    Long selectMaxMsgId(@Param("conversationId") Long conversationId);

    /** 推进已读游标 */
    void markRead(@Param("conversationId") Long conversationId,
                  @Param("userId") Long userId,
                  @Param("lastReadMsgId") Long lastReadMsgId);

    /** 按会话统计未读数（id > 已读游标 且 发送者非本人） */
    List<UnreadStat> selectUnread(@Param("userId") Long userId);

    /** 更新群信息 */
    void updateConversation(@Param("conversationId") Long conversationId,
                                    @Param("name") String name,
                                    @Param("avatar") String avatar,
                                    @Param("notice") String notice);

    /** 撤回消息（仅发送者、2分钟内、未撤回），返回影响行数 */
    int updateRecalled(@Param("messageId") Long messageId,
                                       @Param("userId") Long userId);
}