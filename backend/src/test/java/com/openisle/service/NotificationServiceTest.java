package com.openisle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.openisle.model.*;
import com.openisle.repository.NotificationRepository;
import com.openisle.repository.ReactionRepository;
import com.openisle.repository.UserRepository;
import com.openisle.service.PushNotificationService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class NotificationServiceTest {

  @Test
  void markReadUpdatesOnlyOwnedNotifications() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User user = new User();
    user.setId(1L);
    user.setUsername("alice");
    when(uRepo.findByUsername("alice")).thenReturn(Optional.of(user));

    Notification n1 = new Notification();
    n1.setId(10L);
    n1.setUser(user);
    Notification n2 = new Notification();
    n2.setId(11L);
    n2.setUser(user);
    when(nRepo.findAllById(List.of(10L, 11L))).thenReturn(List.of(n1, n2));

    service.markRead("alice", List.of(10L, 11L));

    assertTrue(n1.isRead());
    assertTrue(n2.isRead());
    verify(nRepo).saveAll(List.of(n1, n2));
  }

  @Test
  void listNotificationsWithoutFilter() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User user = new User();
    user.setId(2L);
    user.setUsername("bob");
    user.setDisabledNotificationTypes(new HashSet<>());
    when(uRepo.findByUsername("bob")).thenReturn(Optional.of(user));

    Notification n = new Notification();
    when(nRepo.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class))).thenReturn(
      new PageImpl<>(List.of(n))
    );

    List<Notification> list = service.listNotifications("bob", null, 0, 10);

    assertEquals(1, list.size());
    verify(nRepo).findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class));
  }

  @Test
  void countUnreadReturnsRepositoryValue() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User user = new User();
    user.setId(3L);
    user.setUsername("carl");
    user.setDisabledNotificationTypes(new HashSet<>());
    when(uRepo.findByUsername("carl")).thenReturn(Optional.of(user));
    when(nRepo.countByUserAndRead(user, false)).thenReturn(5L);

    long count = service.countUnread("carl");

    assertEquals(5L, count);
    verify(nRepo).countByUserAndRead(user, false);
  }

  @Test
  void listNotificationsFiltersDisabledTypes() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User user = new User();
    user.setId(4L);
    user.setUsername("dana");
    when(uRepo.findByUsername("dana")).thenReturn(Optional.of(user));

    Notification n = new Notification();
    when(
      nRepo.findByUserAndTypeNotInOrderByCreatedAtDesc(
        eq(user),
        eq(user.getDisabledNotificationTypes()),
        any(Pageable.class)
      )
    ).thenReturn(new PageImpl<>(List.of(n)));

    List<Notification> list = service.listNotifications("dana", null, 0, 10);

    assertEquals(1, list.size());
    verify(nRepo).findByUserAndTypeNotInOrderByCreatedAtDesc(
      eq(user),
      eq(user.getDisabledNotificationTypes()),
      any(Pageable.class)
    );
  }

  @Test
  void countUnreadFiltersDisabledTypes() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User user = new User();
    user.setId(5L);
    user.setUsername("erin");
    when(uRepo.findByUsername("erin")).thenReturn(Optional.of(user));
    when(
      nRepo.countByUserAndReadAndTypeNotIn(
        eq(user),
        eq(false),
        eq(user.getDisabledNotificationTypes())
      )
    ).thenReturn(2L);

    long count = service.countUnread("erin");

    assertEquals(2L, count);
    verify(nRepo).countByUserAndReadAndTypeNotIn(
      eq(user),
      eq(false),
      eq(user.getDisabledNotificationTypes())
    );
  }

  @Test
  void createRegisterRequestNotificationsDeletesOldOnes() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User admin = new User();
    admin.setId(10L);
    User applicant = new User();
    applicant.setId(20L);

    when(uRepo.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

    service.createRegisterRequestNotifications(applicant, "reason");

    verify(nRepo).deleteByTypeAndFromUser(NotificationType.REGISTER_REQUEST, applicant);
    verify(nRepo).save(any(Notification.class));
  }

  @Test
  void createActivityRedeemNotificationsDeletesOldOnes() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User admin = new User();
    admin.setId(10L);
    User user = new User();
    user.setId(20L);

    when(uRepo.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

    service.createActivityRedeemNotifications(user, "contact");

    verify(nRepo).deleteByTypeAndFromUser(NotificationType.ACTIVITY_REDEEM, user);
    verify(nRepo).save(any(Notification.class));
  }

  @Test
  void createPointRedeemNotificationsDeletesOldOnes() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User admin = new User();
    admin.setId(10L);
    User user = new User();
    user.setId(20L);

    when(uRepo.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

    service.createPointRedeemNotifications(user, "contact");

    verify(nRepo).deleteByTypeAndFromUser(NotificationType.POINT_REDEEM, user);
    verify(nRepo).save(any(Notification.class));
  }

  @Test
  void createNotificationSendsEmailForCommentReply() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User user = new User();
    user.setEmail("a@a.com");
    Post post = new Post();
    post.setId(1L);
    Comment comment = new Comment();
    comment.setId(2L);
    when(nRepo.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

    service.createNotification(
      user,
      NotificationType.COMMENT_REPLY,
      post,
      comment,
      null,
      null,
      null,
      null
    );

    verify(email).sendEmail("a@a.com", "有人回复了你", "https://ex.com/posts/1#comment-2");
    verify(push).sendNotification(eq(user), contains("/posts/1#comment-2"));
  }

  @Test
  void postViewedNotificationDeletesOldOnes() {
    NotificationRepository nRepo = mock(NotificationRepository.class);
    UserRepository uRepo = mock(UserRepository.class);
    ReactionRepository rRepo = mock(ReactionRepository.class);
    EmailSender email = mock(EmailSender.class);
    PushNotificationService push = mock(PushNotificationService.class);
    Executor executor = Runnable::run;
    NotificationService service = new NotificationService(
      nRepo,
      uRepo,
      email,
      push,
      rRepo,
      executor
    );
    org.springframework.test.util.ReflectionTestUtils.setField(
      service,
      "websiteUrl",
      "https://ex.com"
    );

    User owner = new User();
    User viewer = new User();
    Post post = new Post();

    when(nRepo.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

    service.createNotification(
      owner,
      NotificationType.POST_VIEWED,
      post,
      null,
      null,
      viewer,
      null,
      null
    );

    verify(nRepo).deleteByTypeAndFromUserAndPost(NotificationType.POST_VIEWED, viewer, post);
    verify(nRepo).save(any(Notification.class));
  }
}
