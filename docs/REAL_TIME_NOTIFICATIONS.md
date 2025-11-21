# Real-Time Article Published Notifications

## Overview

This document describes the real-time notification system for when articles are published in the blog platform. The system uses WebSocket (STOMP) to deliver instant notifications to connected clients.

## Architecture

### Components

1. **NotificationService** - Core service handling all notification operations
2. **ScheduledPublishService** - Handles scheduled post publishing and notifications
3. **AuthorServices** - Handles immediate post publishing and notifications
4. **WebSocketConfig** - Configures WebSocket endpoints and message broker

### Notification Flow

```
Post Published (Immediate or Scheduled)
    ↓
NotificationService.sendPostPublishedNotification() [Author-specific]
    ↓
NotificationService.broadcastArticlePublishedNotification() [All users]
    ↓
NotificationService.createUserNotification() [Database record]
```

## WebSocket Endpoints

### Connection Endpoint
```
ws://localhost:8080/ws
```

### Subscription Topics

#### 1. Broadcast Article Published Notifications
**Topic:** `/topic/articles/published`

Receives notifications when any article is published. All connected clients receive these messages.

**Message Format:**
```json
{
  "postId": "uuid-string",
  "title": "Article Title",
  "excerpt": "Article excerpt...",
  "slug": "article-slug",
  "public_date": "2024-01-15T10:30:00"
}
```

**Client Subscription (JavaScript):**
```javascript
stompClient.subscribe('/topic/articles/published', function(message) {
  const notification = JSON.parse(message.body);
  console.log('New article published:', notification.title);
  // Update UI with new article
});
```

#### 2. Author-Specific Post Published Notification
**Topic:** `/user/queue/post/published`

Receives notifications specific to the authenticated user about their published posts.

**Message Format:**
```json
{
  "postId": "uuid-string",
  "title": "My Article Title",
  "excerpt": "My article excerpt...",
  "slug": "my-article-slug",
  "public_date": "2024-01-15T10:30:00"
}
```

**Client Subscription (JavaScript):**
```javascript
stompClient.subscribe('/user/queue/post/published', function(message) {
  const notification = JSON.parse(message.body);
  console.log('Your article was published:', notification.title);
  // Show success notification to author
});
```

#### 3. User Notifications (Database Records)
**Topic:** `/user/queue/notifications`

Receives database notification records for the authenticated user.

**Message Format:**
```json
{
  "id": "notification-id",
  "type": "POST_PUBLISHED",
  "title": "Article Published",
  "message": "Your article 'Title' has been published successfully!",
  "isRead": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Client Subscription (JavaScript):**
```javascript
stompClient.subscribe('/user/queue/notifications', function(message) {
  const notification = JSON.parse(message.body);
  console.log('Notification:', notification.message);
  // Display notification in notification center
});
```

## Publishing Scenarios

### Scenario 1: Immediate Publication

When an author creates a post without a scheduled date (or with a date in the past):

1. Post is saved to database with `is_publish = true`
2. `NotificationService.sendPostPublishedNotification()` sends author-specific notification
3. `NotificationService.broadcastArticlePublishedNotification()` broadcasts to all users
4. `NotificationService.createUserNotification()` creates database record

**Triggered from:** `AuthorServices.newPost()`

### Scenario 2: Scheduled Publication

When an author creates a post with a future publication date:

1. Post is saved to database with `is_publish = false`
2. `NotificationService.createUserNotification()` creates "POST_SCHEDULED" notification
3. Scheduled task waits for publication time

**Triggered from:** `AuthorServices.newPost()`

### Scenario 3: Scheduled Post Auto-Publishing

Every 30 seconds, the scheduled task checks for posts due to publish:

1. `ScheduledPublishService.checkForPostPublishing()` runs
2. Finds all posts where `public_date <= now` and `is_publish = false`
3. For each post:
   - Sets `is_publish = true`
   - Sends author-specific notification
   - Broadcasts to all users
   - Creates database notification record

**Triggered from:** `ScheduledPublishService.checkForPostPublishing()` (scheduled every 30 seconds)

## Notification Types

### POST_PUBLISHED
- **When:** Article is published (immediately or via scheduler)
- **Recipients:** Author (user-specific) + All connected users (broadcast)
- **Database:** Yes

### POST_SCHEDULED
- **When:** Article is created with future publication date
- **Recipients:** Author (user-specific)
- **Database:** Yes

## Implementation Details

### NotificationService Methods

#### `broadcastArticlePublishedNotification(PublicArticleNotification notify)`
Broadcasts article published notification to all connected clients.

```java
public void broadcastArticlePublishedNotification(PublicArticleNotification notify) {
    try {
        log.info("Broadcasting article published notification: {}", notify.postId());
        messagingTemplate.convertAndSend("/topic/articles/published", notify);
    } catch (Exception e) {
        log.error("Error broadcasting article published notification", e);
    }
}
```

#### `sendPostPublishedNotification(String username, PublicArticleNotification notify)`
Sends author-specific notification about their published post.

```java
public void sendPostPublishedNotification(String username, PublicArticleNotification notify) {
    try {
        log.info("Sending post published notification to user: {}", username);
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/post/published",
                notify
        );
    } catch (Exception e) {
        log.error("Error sending post published notification to user: {}", username, e);
    }
}
```

#### `createUserNotification(String userId, String type, String title, String message)`
Creates a persistent notification record in the database and sends it to the user.

```java
public Notifications createUserNotification(
        String userId,
        String type,
        String title,
        String message
) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Notifications n = new Notifications();
    n.setUser(user);
    n.setType(type);
    n.setTitle(title);
    n.setMessage(message);
    n.setIsRead(false);
    n.setCreatedAt(LocalDateTime.now());

    Notifications saved = notificationRepository.save(n);

    messagingTemplate.convertAndSendToUser(
            user.getUsername(),
            "/queue/notifications",
            saved
    );

    return saved;
}
```

## Client-Side Implementation Example

### HTML
```html
<!DOCTYPE html>
<html>
<head>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <div id="notifications"></div>
    <div id="articles"></div>
</body>
</html>
```

### JavaScript
```javascript
let stompClient = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame.command);
        
        // Subscribe to broadcast article notifications
        stompClient.subscribe('/topic/articles/published', function(message) {
            const article = JSON.parse(message.body);
            displayNewArticle(article);
        });
        
        // Subscribe to author-specific notifications
        stompClient.subscribe('/user/queue/post/published', function(message) {
            const article = JSON.parse(message.body);
            showAuthorNotification('Your article "' + article.title + '" has been published!');
        });
        
        // Subscribe to user notifications
        stompClient.subscribe('/user/queue/notifications', function(message) {
            const notification = JSON.parse(message.body);
            displayNotification(notification);
        });
    });
}

function displayNewArticle(article) {
    const articlesDiv = document.getElementById('articles');
    const articleElement = document.createElement('div');
    articleElement.innerHTML = `
        <h3>${article.title}</h3>
        <p>${article.excerpt}</p>
        <small>Published: ${article.public_date}</small>
    `;
    articlesDiv.prepend(articleElement);
}

function showAuthorNotification(message) {
    alert(message);
}

function displayNotification(notification) {
    const notificationsDiv = document.getElementById('notifications');
    const notifElement = document.createElement('div');
    notifElement.innerHTML = `
        <strong>${notification.title}</strong>
        <p>${notification.message}</p>
    `;
    notificationsDiv.prepend(notifElement);
}

// Connect when page loads
window.addEventListener('load', connect);
```

## Error Handling

All notification methods include try-catch blocks with logging:

```java
try {
    // Send notification
} catch (Exception e) {
    log.error("Error sending notification", e);
}
```

Errors are logged but don't interrupt the post publishing process. This ensures that notification failures don't prevent articles from being published.

## Performance Considerations

1. **Scheduled Task Frequency:** 30 seconds (configurable via `@Scheduled(fixedRate = 30000)`)
2. **Broadcast Scope:** All connected clients receive broadcast notifications
3. **User-Specific:** User-specific notifications only sent to authenticated users
4. **Database:** All notifications are persisted for audit trail and offline access

## Configuration

### WebSocket Configuration (WebSocketConfig.java)

```java
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable simple broker for topic-based messaging (broadcast)
    config.enableSimpleBroker("/topic", "/queue");
    
    // Set application destination prefix for client-to-server messages
    config.setApplicationDestinationPrefixes("/app");
    
    // Set user destination prefix for user-specific messages
    config.setUserDestinationPrefix("/user");
}
```

### Frontend URL Configuration

The frontend URL is configured in `application.properties`:
```properties
frontend-url=http://localhost:3000
```

## Testing

### Manual Testing with WebSocket Client

1. Connect to WebSocket: `ws://localhost:8080/ws`
2. Subscribe to `/topic/articles/published`
3. Create a new article via API
4. Verify notification is received

### Using curl to Create Article
```bash
curl -X POST http://localhost:8080/api/v1/author/write \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Test Article",
    "excerpt": "Test excerpt",
    "content": "Test content",
    "categories": [1],
    "tags": [],
    "thumbnail": "url",
    "featured": false,
    "public_date": null
  }'
```

## Troubleshooting

### Notifications Not Received

1. **Check WebSocket Connection:**
   - Verify client is connected to `/ws` endpoint
   - Check browser console for connection errors

2. **Check Subscriptions:**
   - Verify client is subscribed to correct topics
   - Check STOMP frame logs

3. **Check Server Logs:**
   - Look for errors in `NotificationService` logs
   - Check `ScheduledPublishService` logs for scheduled publishing

4. **Check CORS:**
   - Verify `frontend-url` in `application.properties` matches client origin
   - Check WebSocket CORS configuration

### Scheduled Posts Not Publishing

1. **Check Scheduled Task:**
   - Verify `ScheduledPublishService` is running
   - Check logs for `checkForPostPublishing()` execution

2. **Check Post Date:**
   - Verify `public_date` is in the past
   - Check database for post status

3. **Check Database:**
   - Verify post exists in database
   - Check `is_publish` flag status

## Future Enhancements

1. **Notification Preferences:** Allow users to customize notification types
2. **Email Notifications:** Send email when articles are published
3. **Push Notifications:** Send push notifications to mobile apps
4. **Notification History:** Implement notification history and archiving
5. **Batch Notifications:** Group multiple notifications for efficiency
6. **Retry Logic:** Implement retry mechanism for failed notifications
