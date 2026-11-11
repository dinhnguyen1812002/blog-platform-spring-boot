package com.Nguyen.blogplatform.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {

    private static String clean(String html) {
        return HtmlSanitizer.sanitizePostContent(html);
    }

    @Test
    void removesScriptsAndEventHandlers() {
        String out = clean("<p>hi</p><script>alert(1)</script><img src=x onerror=alert(1)>");
        assertThat(out).contains("<p>hi</p>");
        assertThat(out).doesNotContain("<script", "onerror", "alert(1)</script>");
    }

    @Test
    void removesJavascriptUrls() {
        assertThat(clean("<a href=\"javascript:alert(1)\">x</a>")).doesNotContain("javascript:");
        assertThat(clean("<img src=\"javascript:alert(1)\">")).doesNotContain("javascript:");
    }

    @Test
    void removesIframes() {
        assertThat(clean("<iframe src=\"https://evil.example\"></iframe>")).doesNotContain("iframe");
    }

    @Test
    void keepsTiptapMarkup() {
        String in = "<h2 style=\"text-align: center\">Title</h2>"
                + "<p><strong>b</strong> <em>i</em> <u>u</u> <s>s</s> <mark data-color=\"#ff0\" style=\"background-color: #ff0\">m</mark></p>"
                + "<pre><code class=\"language-java\">int a = 1;</code></pre>"
                + "<ul data-type=\"taskList\"><li data-checked=\"true\" data-type=\"taskItem\"><label><input type=\"checkbox\" checked></label><div><p>done</p></div></li></ul>"
                + "<p><a href=\"https://example.com\" target=\"_blank\">link</a></p>"
                + "<img src=\"https://cdn.example.com/a.png\" alt=\"a\">";
        String out = clean(in);

        assertThat(out).contains("<h2", "text-align:center", "<strong>b</strong>", "<mark",
                "class=\"language-java\"", "data-type=\"taskList\"", "data-checked=\"true\"",
                "type=\"checkbox\"", "href=\"https://example.com\"", "rel=\"noopener noreferrer\"",
                "src=\"https://cdn.example.com/a.png\"");
    }

    @Test
    void handlesNullAndEmpty() {
        assertThat(clean(null)).isNull();
        assertThat(clean("")).isEmpty();
    }
}
