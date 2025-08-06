# Kế hoạch triển khai: Cập nhật lượt xem bài viết Real-Time bằng WebSocket

Đây là tài liệu mô tả các bước cần thiết để triển khai tính năng cập nhật số lượt xem của một bài viết trong thời gian thực cho tất cả người dùng đang xem bài viết đó.

Công nghệ sử dụng: **Spring Boot WebSocket** và **STOMP** protocol.

## Luồng hoạt động

1.  **Client (Trình duyệt):** Khi người dùng truy cập vào một trang chi tiết bài viết, trình duyệt sẽ tự động mở một kết nối WebSocket tới server và "lắng nghe" trên một kênh (topic) dành riêng cho bài viết đó (ví dụ: `/topic/posts/{slug}/views`).
2.  **Server (Backend):** Mỗi khi API `GET /api/posts/{slug}` được gọi (tức là có người xem bài viết), `PostService` sẽ:
    a. Tăng `viewCount` trong cơ sở dữ liệu.
    b. Lấy số `viewCount` mới nhất.
    c. Gửi một tin nhắn chứa số `viewCount` mới này tới kênh WebSocket tương ứng.
3.  **Client (Trá»�nh duyá»�t):** Tất cả các trình duyệt đang lắng nghe trên kênh đó sẽ nhận được tin nhắn. Một đoạn mã JavaScript sẽ ngay lập tức cập nhật con số hiển thị trên giao diện người dùng mà không cần tải lại trang.

---

## Các bước triển khai

### Phần 1: Backend (Spring Boot)

Mục tiêu: Sửa đổi `PostService` để gửi tin nhắn WebSocket sau khi tăng lượt xem.

1.  **Inject `SimpMessagingTemplate`:**
    *   Thêm `SimpMessagingTemplate` vào `PostService` thông qua constructor injection. Đây là công cụ chính để gửi tin nhắn.

    ```java
    // Trong file: src/main/java/com/Nguyen/blogplatform/service/PostService.java
    
    @Service
    @RequiredArgsConstructor
    public class PostService {
        // ... các dependency khác
        private final SimpMessagingTemplate messagingTemplate;
    
        // ...
    }
    ```

2.  **Gửi tin nhắn WebSocket:**
    *   Trong phương thức `getPostBySlug` của `PostService`, sau khi đã tăng view và lấy được `updatedPost`, thêm logic để gửi tin nhắn.

    ```java
    // Trong phương thức getPostBySlug
    
    // ... sau khi có được updatedPost
    var updatedPost = postRepository.findById(post.getId())...
    
    // Gửi số view mới qua WebSocket
    messagingTemplate.convertAndSend("/topic/posts/" + slug + "/views", updatedPost.getViewCount());
    
    return postMapper.toPostResponseWithComments(updatedPost, getCurrentUser(), savedPostRepository);
    ```

### Phần 2: Frontend (JavaScript)

Mục tiêu: Viết mã JavaScript trên trang chi tiết bài viết để kết nối WebSocket và cập nhật giao diện.

1.  **Thêm thư viện:**
    *   Đảm bảo trang HTML của bạn đã nhúng 2 thư viện `SockJS` và `Stomp.js`.

    ```html
    <!-- Ví dụ trong file post-detail.html -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    ```

2.  **Viết mã kết nối và lắng nghe:**
    *   Thêm một đoạn script để xử lý logic WebSocket.

    ```javascript
    // Giả sử bạn có một thẻ để hiển thị view, ví dụ: <span id="view-count">123</span>
    // Và slug của bài viết có sẵn trong một biến JavaScript, ví dụ: const postSlug = "ten-bai-viet";
    
    document.addEventListener("DOMContentLoaded", function() {
        const viewCountElement = document.getElementById('view-count');
        const postSlug = "your-post-slug"; // Cần lấy slug của bài viết hiện tại
    
        // 1. Tạo kết nối
        const socket = new SockJS('/ws'); // Endpoint WebSocket đã cấu hình trong WebSocketConfig
        const stompClient = Stomp.over(socket);
    
        // 2. Kết nối tới server
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
    
            // 3. Đăng ký (subscribe) vào kênh của bài viết
            stompClient.subscribe('/topic/posts/' + postSlug + '/views', function (message) {
                // 4. Callback: Xử lý khi nhận được tin nhắn
                const newViewCount = message.body;
                console.log('New view count received: ' + newViewCount);
                if (viewCountElement) {
                    viewCountElement.innerText = newViewCount;
                }
            });
        });
    
        // Xử lý khi mất kết nối (tùy chọn)
        stompClient.onclose = function() {
            console.log('Connection closed');
        };
    });
    ```