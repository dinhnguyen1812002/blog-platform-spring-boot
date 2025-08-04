# Kế hoạch Nâng cấp Trình soạn thảo (Editor)

**Mục tiêu:** Hỗ trợ đồng thời hai định dạng soạn thảo: **Rich Text (WYSIWYG)** và **Markdown**.

---

### Phần 1: Thay đổi ở Backend (Spring Boot)

Mục tiêu là để model `Post` có thể lưu được cả hai loại nội dung. Cách tốt nhất là thêm một trường để phân loại nội dung.

**Bước 1: Cập nhật Model `Post`**

1.  Tạo một `enum` để định nghĩa loại nội dung.
    *   Tạo file `src/main/java/com/Nguyen/blogplatform/Enum/ContentType.java`:
        ```java
        package com.Nguyen.blogplatform.Enum;

        public enum ContentType {
            RICHTEXT,
            MARKDOWN
        }
        ```
2.  Thêm trường `contentType` vào model `Post.java`.
    *   Mở file `src/main/java/com/Nguyen/blogplatform/model/Post.java`.
    *   Thêm vào các trường thuộc tính:
        ```java
        import com.Nguyen.blogplatform.Enum.ContentType;
        import jakarta.persistence.EnumType;
        import jakarta.persistence.Enumerated;

        // ... bên trong class Post

        @Enumerated(EnumType.STRING)
        @Column(name = "content_type", nullable = false)
        private ContentType contentType;
        ```
    *   **Quan trọng:** Bạn cần đảm bảo `nullable = false` và có thể đặt một giá trị mặc định nếu cần, ví dụ `private ContentType contentType = ContentType.RICHTEXT;`.

**Bước 2: Cập nhật DTOs (`PostRequest` và `PostResponse`)**

1.  **`PostRequest.java`**: Thêm trường `contentType` để frontend có thể gửi lên loại nội dung.
    ```java
    // ... bên trong class PostRequest
    private ContentType contentType;
    ```
2.  **`PostResponse.java`**: Thêm trường `contentType` để API trả về cho frontend biết cách hiển thị.
    ```java
    // ... bên trong class PostResponse
    private ContentType contentType;
    ```

**Bước 3: Cập nhật Service Logic (`AuthorServices.java`)**

1.  Trong phương thức `newPost` và `updatePost`, bạn cần đọc `contentType` từ `postRequest` và set nó cho đối tượng `Post` trước khi lưu vào database.
    ```java
    // Ví dụ trong phương thức newPost
    Post post = new Post();
    // ... set các trường khác
    post.setContentType(postRequest.getContentType()); // Thêm dòng này
    postRepository.save(post);
    ```

---

### Phần 2: Thay đổi ở Frontend (React)

Mục tiêu là cung cấp lựa chọn editor cho người dùng và hiển thị nội dung đúng định dạng.

**Bước 1: Cập nhật trang Viết/Sửa bài (`/author/posts/new`, `/author/posts/edit/:id`)**

1.  **Thêm lựa chọn Editor:**
    *   Tạo một component chuyển đổi (toggle switch hoặc tabs) cho phép người dùng chọn giữa "Rich Text Editor" và "Markdown".
2.  **Cài đặt thư viện cần thiết:**
    ```bash
    # Thư viện để render Markdown
    npm install react-markdown

    # Thư viện editor cho Markdown (tùy chọn, có thể dùng textarea thường)
    npm install @uiw/react-md-editor

    # Thư viện editor cho Rich Text (nếu chưa có)
    npm install react-quill
    ```
3.  **Hiển thị Editor tương ứng:**
    *   Dựa vào state của toggle switch, hiển thị một trong hai editor:
        ```tsx
        const [editorType, setEditorType] = useState<'richtext' | 'markdown'>('richtext');

        //...

        {editorType === 'richtext' ? (
          <ReactQuill theme="snow" value={content} onChange={setContent} />
        ) : (
          <MDEditor value={content} onChange={setContent} />
        )}
        ```
4.  **Gửi dữ liệu lên API:**
    *   Khi người dùng nhấn "Lưu", bạn cần gửi cả `content` và `contentType` (`'RICHTEXT'` hoặc `'MARKDOWN'`) lên backend.

**Bước 2: Cập nhật trang Chi tiết bài viết (`/posts/:slug`)**

1.  **Đọc `contentType` từ API:** Khi nhận được dữ liệu bài viết, kiểm tra giá trị của `post.contentType`.
2.  **Hiển thị nội dung đúng cách:**
    *   Nếu `contentType` là `MARKDOWN`, sử dụng `react-markdown` để hiển thị.
        ```tsx
        import ReactMarkdown from 'react-markdown';

        // ...
        {post.contentType === 'MARKDOWN' ? (
          <ReactMarkdown>{post.content}</ReactMarkdown>
        ) : (
          // ... xem mục dưới
        )}
        ```
    *   Nếu `contentType` là `RICHTEXT`, nội dung sẽ là HTML.
        *   **CẢNH BÁO BẢO MẬT:** Không bao giờ render HTML trực tiếp từ người dùng mà không qua xử lý để tránh tấn công XSS.
        *   Cài đặt thư viện `dompurify` để làm sạch HTML:
            ```bash
            npm install dompurify
            npm install -D @types/dompurify
            ```
        *   Sử dụng nó trước khi render:
            ```tsx
            import DOMPurify from 'dompurify';

            // ...
            {post.contentType === 'RICHTEXT' && (
              <div
                dangerouslySetInnerHTML={{
                  __html: DOMPurify.sanitize(post.content),
                }}
              />
            )}
            ```
