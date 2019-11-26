package red.fuyun.chat.entiy;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *与前台交互用户信息
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatcheDo {

    /**
     * 是否匹配成功 目前是布尔型 可以设置枚举值为不同状态 true 匹配成功
     */
    private Boolean success;

    /**
     * 是否匹配成功后用户的信息
     */
    private UserInfo info;

}
