package meea.licenta.greeny.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangePasswordRequest {
    public String oldPassword;
    public String newPassword;
}
