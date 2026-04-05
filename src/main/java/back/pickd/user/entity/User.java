package back.pickd.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users") // User는 예약어일 수 있으므로 명시적 테이블명 권장
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column
    private String picture;

    @Column
    private LocalDateTime lastLoginDate;

    @Builder
    public User(String email, String name, String picture) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.lastLoginDate = LocalDateTime.now();
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        this.lastLoginDate = LocalDateTime.now();
        return this;
    }
}