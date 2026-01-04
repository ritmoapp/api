package com.wellness.ritmo.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "user",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 200)
    private String password;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "is_user_on", nullable = false)
    private Boolean isUserOn = true;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    /**
     * Lado inverso do relacionamento 1–1
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = false
    )
    private UserProfile profile;

}
