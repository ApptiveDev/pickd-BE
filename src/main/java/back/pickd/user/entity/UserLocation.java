package back.pickd.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_locations")
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String currentResidence;

    @ElementCollection
    @CollectionTable(name = "user_desired_locations",
            joinColumns = @JoinColumn(name = "user_location_id"),
            indexes = @Index(name = "idx_user_desired_locations", columnList = "user_location_id"))
    @Column(name = "location")
    private List<String> desiredLocations = new ArrayList<>();

    @Column
    private String detailedAddress;

    public void update(String currentResidence, List<String> desiredLocations, String detailedAddress) {
        this.currentResidence = currentResidence;
        this.desiredLocations = desiredLocations;
        this.detailedAddress = detailedAddress;
    }

    @Builder
    public UserLocation(User user, String currentResidence, List<String> desiredLocations, String detailedAddress) {
        this.user = user;
        this.currentResidence = currentResidence;
        this.desiredLocations = desiredLocations;
        this.detailedAddress = detailedAddress;
    }
}
