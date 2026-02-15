package dto;
import lombok.Data;

@Data
public class GeneratorRequest {

    private int length;
    private boolean includeUppercase;
    private boolean includeLowercase;
    private boolean includeNumbers;
    private boolean includeSpecial;
}
