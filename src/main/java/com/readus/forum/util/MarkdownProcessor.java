package com.readus.forum.util;


import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

/**
 * Converts user markdown to safe HTML.
 *
 * <p>The commonmark renderer already escapes all user-supplied text and only emits tags that come
 * from markdown syntax, so its output is safe as-is (escaping it again would make the browser
 * display literal {@code <p>} tags instead of rendering them). The one vector commonmark does not
 * block by default is dangerous URL schemes in link/image destinations ({@code javascript:},
 * {@code data:}, ...), which are neutralized here before rendering.
 */
@Component
public class MarkdownProcessor {

    private static final String UNSAFE_URL_FALLBACK = "#";

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public String toSafeHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node document = parser.parse(markdown);
        sanitizeNodes(document);
        return renderer.render(document);
    }

    private void sanitizeNodes(Node node) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Link link) {
                link.setDestination(sanitizeUrl(link.getDestination()));
            } else if (child instanceof Image image) {
                image.setDestination(sanitizeUrl(image.getDestination()));
            }
            sanitizeNodes(child);
        }
    }

    /** Replaces dangerous URL schemes with a harmless placeholder. */
    private String sanitizeUrl(String url) {
        if (url == null) {
            return null;
        }
        // Strip control chars/whitespace so "java\tscript:" cannot disguise the scheme
        String normalized = url.replaceAll("[\\p{Cntrl}\\s]", "");
        if (normalized.regionMatches(true, 0, "javascript:", 0, "javascript:".length())
                || normalized.regionMatches(true, 0, "data:", 0, "data:".length())
                || normalized.regionMatches(true, 0, "vbscript:", 0, "vbscript:".length())) {
            return UNSAFE_URL_FALLBACK;
        }
        return url;
    }
}
