package ru.burdak.game.activity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.burdak.game.user.model.User;

import java.time.Instant;

@Entity
@Table(name = "activities")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {
  public Activity(User user, long activity, Instant receivedAt) {
    this.user = user;
    this.activity = activity;
    this.receivedAt = receivedAt;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "activity", nullable = false)
  private long activity;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;
}
