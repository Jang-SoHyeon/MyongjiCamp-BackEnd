package com.won.myongjiCamp.model.board;

import com.won.myongjiCamp.model.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "board_type")
public abstract class Board {

    @Column(name="board_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

//    private Integer scrapCount=0; //스크랩 수

    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createDate = LocalDateTime.now();

    @Column(name = "modified_date")
    @UpdateTimestamp
    private LocalDateTime modifiedDate = LocalDateTime.now();


}

