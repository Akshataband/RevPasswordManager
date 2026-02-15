package entities;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SecurityQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String answer;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
