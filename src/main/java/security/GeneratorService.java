package security;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class GeneratorService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+";

    public String generate(int length,
                           boolean upper,
                           boolean lower,
                           boolean numbers,
                           boolean special) {

        StringBuilder pool = new StringBuilder();

        if (upper) pool.append(UPPER);
        if (lower) pool.append(LOWER);
        if (numbers) pool.append(NUMBERS);
        if (special) pool.append(SPECIAL);

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(pool.length());
            password.append(pool.charAt(index));
        }

        return password.toString();
    }
}
