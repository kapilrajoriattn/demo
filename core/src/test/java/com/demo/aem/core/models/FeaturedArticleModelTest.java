/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.demo.aem.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.demo.aem.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Unit tests for {@link FeaturedArticleModel}.
 */
@ExtendWith(AemContextExtension.class)
class FeaturedArticleModelTest {

    private static final String RESOURCE_TYPE = "demo/components/featured-article";

    private final AemContext context = AppAemContext.newAemContext();

    private Page page;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(FeaturedArticleModel.class);
        page = context.create().page("/content/mypage");
    }

    @Test
    void testWithCompleteData() {
        Resource resource = context.create().resource(page, "article",
            "sling:resourceType", RESOURCE_TYPE,
            "image", "/content/dam/demo/river.jpg",
            "title", "Featured Article",
            "heading", "<p>Camping</p>",
            "description", "<p>Text</p>",
            "ctaText", "Full article",
            "ctaLink", "/content/mypage");

        FeaturedArticleModel model = resource.adaptTo(FeaturedArticleModel.class);

        assertNotNull(model);
        assertEquals("/content/dam/demo/river.jpg", model.getImage());
        assertEquals("Featured Article", model.getTitle());
        assertEquals("<p>Camping</p>", model.getHeading());
        assertEquals("<p>Text</p>", model.getDescription());
        assertEquals("Full article", model.getCtaText());
        assertEquals("/content/mypage.html", model.getCtaLink());
        assertTrue(model.hasCta());
        assertTrue(model.hasContent());
    }

    @Test
    void testWhenEmpty() {
        Resource resource = context.create().resource(page, "article",
            "sling:resourceType", RESOURCE_TYPE);

        FeaturedArticleModel model = resource.adaptTo(FeaturedArticleModel.class);

        assertNotNull(model);
        assertNull(model.getImage());
        assertNull(model.getTitle());
        assertNull(model.getHeading());
        assertNull(model.getDescription());
        assertNull(model.getCtaText());
        assertNull(model.getCtaLink());
        assertFalse(model.hasCta());
        assertFalse(model.hasContent());
    }

    @Test
    void testWithPartialData_BlankValuesIgnored() {
        Resource resource = context.create().resource(page, "article",
            "sling:resourceType", RESOURCE_TYPE,
            "title", "   ",
            "heading", "<p>Only heading</p>");

        FeaturedArticleModel model = resource.adaptTo(FeaturedArticleModel.class);

        assertNotNull(model);
        assertNull(model.getTitle());
        assertEquals("<p>Only heading</p>", model.getHeading());
        assertTrue(model.hasContent());
    }

    @Test
    void testCtaRequiresTextAndLink() {
        FeaturedArticleModel textOnly = context.create().resource(page, "textOnly",
            "ctaText", "Read").adaptTo(FeaturedArticleModel.class);
        FeaturedArticleModel linkOnly = context.create().resource(page, "linkOnly",
            "ctaLink", "/content/mypage").adaptTo(FeaturedArticleModel.class);

        assertNotNull(textOnly);
        assertNotNull(linkOnly);
        assertFalse(textOnly.hasCta());
        assertFalse(textOnly.hasContent());
        assertFalse(linkOnly.hasCta());
        assertFalse(linkOnly.hasContent());
    }

    @Test
    void testGetCtaLink_Variants() {
        assertEquals("/content/mypage.html", ctaLinkOf("a", "/content/mypage"));
        assertEquals("/content/mypage.html", ctaLinkOf("b", "/content/mypage.html"));
        assertEquals("/content/dam/demo/file.pdf", ctaLinkOf("c", "/content/dam/demo/file.pdf"));
        assertEquals("/content/dam/demo/folder", ctaLinkOf("d", "/content/dam/demo/folder"));
        assertEquals("/content/my.site/page.html", ctaLinkOf("e", "/content/my.site/page"));
        assertEquals("#", ctaLinkOf("f", "#"));
    }

    private String ctaLinkOf(String name, String link) {
        FeaturedArticleModel model = context.create().resource(page, name,
            "ctaText", "Go", "ctaLink", link).adaptTo(FeaturedArticleModel.class);
        assertNotNull(model);
        return model.getCtaLink();
    }
}
