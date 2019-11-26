package red.fuyun.chat.entiy;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *用户信息
 */
@Data
@NoArgsConstructor
public class UserInfo {
    private String sex;
    private Integer age;
    private String token;

    public UserInfo(String sex, Integer age) {
        this(sex,age,null);
    }

    public UserInfo(String sex, Integer age, String token) {
        this.sex = sex;
        this.age = age;
        this.token = token;
    }
}
