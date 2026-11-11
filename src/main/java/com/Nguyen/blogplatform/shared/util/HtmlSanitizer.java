package com.Nguyen.blogplatform.shared.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

/**
 * Sanitizes user-authored rich-text HTML (TipTap editor output) before it is
 * persisted. Allows only the markup the editor produces: headings, lists,
 * task lists, code blocks, links, images, highlight/colour/alignment styles.
 * Strips scripts, event handlers, iframes and javascript:/data: URLs.
 */
public final class HtmlSanitizer {

    /** Plain CSS class names only (e.g. "language-java", "hljs-keyword"). */
    private static final Pattern SAFE_CLASS = Pattern.compile("[a-zA-Z0-9_\\- ]{1,100}");

    private static final PolicyFactory POST_CONTENT_POLICY = new HtmlPolicyBuilder()
            .allowElements(
                    "p", "br", "h1", "h2", "h3", "h4", "h5", "h6",
                    "strong", "b", "em", "i", "u", "s", "strike", "del",
                    "code", "pre", "blockquote", "hr",
                    "ul", "ol", "li", "label", "div", "span", "mark", "sub", "sup",
                    "table", "thead", "tbody", "tr", "th", "td",
                    "a", "img", "input")
            // Links: only http(s), mailto and relative URLs
            .allowUrlProtocols("http", "https", "mailto")
            .allowAttributes("href", "title").onElements("a")
            .allowAttributes("target").matching(false, "_blank").onElements("a")
            .requireRelsOnLinks("noopener", "noreferrer")
            // Images: http(s) and relative only (no data:/javascript:)
            .allowAttributes("src", "alt", "title", "width", "height").onElements("img")
            // TipTap: code-block language, task lists, highlight colours
            .allowAttributes("class").matching(SAFE_CLASS)
                    .onElements("code", "pre", "span", "div", "p", "ul", "ol", "li", "mark", "img")
            .allowAttributes("data-type").onElements("ul", "li", "div")
            .allowAttributes("data-checked").onElements("li")
            .allowAttributes("data-color").onElements("mark")
            .allowAttributes("start").onElements("ol")
            .allowAttributes("colspan", "rowspan").onElements("th", "td")
            // Task-list checkboxes only
            .allowAttributes("type").matching(false, "checkbox").onElements("input")
            .allowAttributes("checked", "disabled").onElements("input")
            // color / background-color / text-align from TextStyle, Highlight, TextAlign
            .allowStyling()
            .toFactory();

    private HtmlSanitizer() {
    }

    public static String sanitizePostContent(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return POST_CONTENT_POLICY.sanitize(html);
    }
}
