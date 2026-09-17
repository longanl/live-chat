package com.xuziran.livechat.mapper;

import com.xuziran.livechat.model.dto.PasswordDTO;
import com.xuziran.livechat.model.dto.RegisterDTO;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.UserDetailVO;
import com.xuziran.livechat.model.vo.UserSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User login(String username);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RegisterDTO registerDTO);

    List<User> queryOnlineUser();

    void updateStatus(User user);

    Long getIdByUsername(String username);

    void update(User user);

    User getById(Long id);

    Long count();

    /** 搜索用户总数（用户名/昵称模糊匹配，排除自己） */
    Long countSearch(@Param("keyword") String keyword, @Param("userId") Long userId);

    /** 搜索用户分页（含当前用户视角的关系字段） */
    List<UserSearchVO> selectSearch(@Param("keyword") String keyword, @Param("userId") Long userId,
                                    @Param("offset") Integer offset, @Param("size") Integer size);

    /** 用户详情（含当前用户视角的关系字段） */
    UserDetailVO selectUserDetail(@Param("id") Long id, @Param("userId") Long userId);
}
