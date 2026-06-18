package org.example.dongnegadeuk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "placements")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Placements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placementId;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false)
    private Integer zOrder;

    @Column(precision = 5, scale = 2, columnDefinition = "DECIMAL(5,2) DEFAULT 1.0")
    private BigDecimal scale;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean topBottom = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean leftRight = false;

    // FK
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_item_id", nullable = false, unique = true)
    private UserItems userItem;
}