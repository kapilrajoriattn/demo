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
 * Unit tests for {@link RecentArticlesModel} and {@link ArticleItem}.
 */
@ExtendWith(AemContextExtension.class)
class RecentArticlesModelTest {

    private static final String RESOURCE_TYPE = "demo/components/recent-articles";
    private static final String ARTICLES = "/content/demo/articles";

    private final AemContext context = AppAemContext.newAemContext();

    private Page host;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(RecentArticlesModel.class);
        host = context.create().page("/content/demo/home");
        context.create().page(ARTICLES);
    }

    private RecentArticlesModel model(Object... properties) {
        Object[] all = new Object[properties.length + 2];
        all[0] = "sling:resourceType";
        all[1] = RESOURCE_TYPE;
        System.arraycopy(properties, 0, all, 2, properties.length);
        Resource resource = context.create().resource(host, "articles-" + System.nanoTime(), all);
        RecentArticlesModel model = resource.adaptTo(RecentArticlesModel.class);
        assertNotNull(model);
        return model;
    }

    @Test
    void testGetArticles_ListsDirectChildPagesInOrder() {
        context.create().page(ARTICLES + "/skiing", null, "jcr:title", "Ski Touring", "jcr:description", "About skiing");
        context.create().page(ARTICLES + "/surfing", null, "jcr:title", "Arctic Surfing");
        context.create().page(ARTICLES + "/surfing/deep-page", null, "jcr:title", "Not a direct child");

        RecentArticlesModel model = model("title", "Recent Articles", "path", ARTICLES);

        assertEquals("Recent Articles", model.getTitle());
        assertTrue(model.hasContent());
        assertEquals(2, model.getArticles().size());

        ArticleItem first = model.getArticles().get(0);
        assertEquals("Ski Touring", first.getTitle());
        assertEquals("About skiing", first.getDescription());
        assertEquals(ARTICLES + "/skiing.html", first.getUrl());
        assertNull(first.getThumbnail());
        assertEquals("Arctic Surfing", model.getArticles().get(1).getTitle());
        assertNull(model.getArticles().get(1).getDescription());
    }

    @Test
    void testGetThumbnail_PrefersFeaturedImageThenFallsBackToImage() {
        context.create().page(ARTICLES + "/featured", null, "jcr:title", "Featured");
        context.create().resource(ARTICLES + "/featured/jcr:content/cq:featuredimage",
            "fileReference", "/content/dam/demo/featured.jpg");
        context.create().resource(ARTICLES + "/featured/jcr:content/image",
            "fileReference", "/content/dam/demo/classic.jpg");
        context.create().page(ARTICLES + "/classic", null, "jcr:title", "Classic");
        context.create().resource(ARTICLES + "/classic/jcr:content/image",
            "fileReference", "/content/dam/demo/classic.jpg");

        RecentArticlesModel model = model("path", ARTICLES);

        assertEquals(2, model.getArticles().size());
        assertEquals("/content/dam/demo/featured.jpg", model.getArticles().get(0).getThumbnail());
        assertEquals("/content/dam/demo/classic.jpg", model.getArticles().get(1).getThumbnail());
    }

    @Test
    void testGetArticles_WhenPathHasNoChildren() {
        RecentArticlesModel model = model("path", ARTICLES);

        assertTrue(model.getArticles().isEmpty());
        assertFalse(model.hasContent());
    }

    @Test
    void testGetArticles_WhenPathIsMissingOrInvalid() {
        RecentArticlesModel noPath = model("title", "Recent Articles");
        RecentArticlesModel unknownPath = model("path", "/content/demo/does-not-exist");

        assertNotNull(noPath.getArticles());
        assertFalse(noPath.hasContent());
        assertNotNull(unknownPath.getArticles());
        assertFalse(unknownPath.hasContent());
    }

    @Test
    void testCta() {
        RecentArticlesModel full = model("path", ARTICLES, "ctaText", "All Articles", "ctaLink", "/content/demo/all");
        RecentArticlesModel textOnly = model("path", ARTICLES, "ctaText", "All Articles");
        RecentArticlesModel linkOnly = model("path", ARTICLES, "ctaLink", "/content/demo/all");

        assertTrue(full.hasCta());
        assertEquals("All Articles", full.getCtaText());
        assertEquals("/content/demo/all.html", full.getCtaLink());
        assertFalse(textOnly.hasCta());
        assertFalse(linkOnly.hasCta());
    }

    @Test
    void testGetCtaLink_Variants() {
        assertEquals("/content/demo/all.html", model("ctaLink", "/content/demo/all.html").getCtaLink());
        assertEquals("/content/dam/demo/file.pdf", model("ctaLink", "/content/dam/demo/file.pdf").getCtaLink());
        assertEquals("/content/my.site/page.html", model("ctaLink", "/content/my.site/page").getCtaLink());
        assertNull(model("ctaLink", "  ").getCtaLink());
    }
}
