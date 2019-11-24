package red.fuyun.chat.entiy;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatcheDto {
    private Boolean success;
    private UserInfo info;

}
