package com.readus.forum.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;

@Component
public class MarkdownProcessor {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public String toSafeHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node document = parser.parse(markdown);
        String html = renderer.render(document);

        return Encode.forHtmlContent(html);
    }
}