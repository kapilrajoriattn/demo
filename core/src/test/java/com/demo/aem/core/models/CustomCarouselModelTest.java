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
 * Unit tests for {@link CustomCarouselModel} and {@link SlideItem}.
 */
@ExtendWith(AemContextExtension.class)
class CustomCarouselModelTest {

    private static final String RESOURCE_TYPE = "demo/components/custom-carousel";

    private final AemContext context = AppAemContext.newAemContext();

    private Page page;
    private Resource component;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(CustomCarouselModel.class, SlideItem.class);
        page = context.create().page("/content/mypage");
        component = context.create().resource(page, "carousel", "sling:resourceType", RESOURCE_TYPE);
    }

    @Test
    void testGetSlides_WithMultipleItems() {
        context.create().resource(component, "slides/item0",
            "image", "/content/dam/demo/one.jpg",
            "heading", "<p>One</p>",
            "description", "<p>First</p>",
            "ctaText", "Read",
            "ctaLink", "/content/mypage");
        context.create().resource(component, "slides/item1",
            "heading", "<p>Two</p>");

        CustomCarouselModel model = component.adaptTo(CustomCarouselModel.class);

        assertNotNull(model);
        assertEquals(2, model.getSlides().size());
        assertTrue(model.hasContent());
        assertTrue(model.isNavigable());

        SlideItem first = model.getSlides().get(0);
        assertEquals("/content/dam/demo/one.jpg", first.getImage());
        assertEquals("<p>One</p>", first.getHeading());
        assertEquals("<p>First</p>", first.getDescription());
        assertEquals("Read", first.getCtaText());
        assertEquals("/content/mypage.html", first.getCtaLink());
        assertTrue(first.hasCta());
        assertEquals("<p>Two</p>", model.getSlides().get(1).getHeading());
    }

    @Test
    void testGetSlides_WithSingleItem() {
        context.create().resource(component, "slides/item0", "heading", "<p>Only</p>");

        CustomCarouselModel model = component.adaptTo(CustomCarouselModel.class);

        assertNotNull(model);
        assertEquals(1, model.getSlides().size());
        assertTrue(model.hasContent());
        assertFalse(model.isNavigable());
    }

    @Test
    void testGetSlides_WhenNoItems() {
        CustomCarouselModel model = component.adaptTo(CustomCarouselModel.class);

        assertNotNull(model);
        assertNotNull(model.getSlides());
        assertTrue(model.getSlides().isEmpty());
        assertFalse(model.hasContent());
        assertFalse(model.isNavigable());
    }

    @Test
    void testGetSlides_SkipsEmptyItems() {
        context.create().resource(component, "slides/item0", "heading", "   ");
        context.create().resource(component, "slides/item1", "heading", "<p>Kept</p>");

        CustomCarouselModel model = component.adaptTo(CustomCarouselModel.class);

        assertNotNull(model);
        assertEquals(1, model.getSlides().size());
        assertEquals("<p>Kept</p>", model.getSlides().get(0).getHeading());
    }

    @Test
    void testSlideItem_WhenEmpty() {
        Resource slideResource = context.create().resource(component, "slides/item0");

        SlideItem slide = slideResource.adaptTo(SlideItem.class);

        assertNotNull(slide);
        assertNull(slide.getImage());
        assertNull(slide.getHeading());
        assertNull(slide.getDescription());
        assertNull(slide.getCtaText());
        assertNull(slide.getCtaLink());
        assertFalse(slide.hasCta());
        assertFalse(slide.hasContent());
    }

    @Test
    void testSlideItem_CtaRequiresTextAndLink() {
        SlideItem textOnly = context.create().resource(component, "slides/item0",
            "ctaText", "Read").adaptTo(SlideItem.class);
        SlideItem linkOnly = context.create().resource(component, "slides/item1",
            "ctaLink", "/content/mypage").adaptTo(SlideItem.class);

        assertNotNull(textOnly);
        assertNotNull(linkOnly);
        assertFalse(textOnly.hasCta());
        assertFalse(textOnly.hasContent());
        assertFalse(linkOnly.hasCta());
        assertFalse(linkOnly.hasContent());
    }

    @Test
    void testGetCtaLink_Variants() {
        assertEquals("/content/mypage.html", ctaLinkOf("item0", "/content/mypage"));
        assertEquals("/content/mypage.html", ctaLinkOf("item1", "/content/mypage.html"));
        assertEquals("/content/dam/demo/file.pdf", ctaLinkOf("item2", "/content/dam/demo/file.pdf"));
        assertEquals("/content/dam/demo/folder", ctaLinkOf("item3", "/content/dam/demo/folder"));
        assertEquals("/content/my.site/page.html", ctaLinkOf("item4", "/content/my.site/page"));
    }

    private String ctaLinkOf(String name, String link) {
        SlideItem slide = context.create().resource(component, "slides/" + name,
            "ctaText", "Go", "ctaLink", link).adaptTo(SlideItem.class);
        assertNotNull(slide);
        return slide.getCtaLink();
    }
}
