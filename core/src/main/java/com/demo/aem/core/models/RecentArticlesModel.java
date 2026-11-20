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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Sling Model for the Recent Articles component.
 * Lists the direct child pages of the authored path, in their authored order.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RecentArticlesModel {

    private static final String CONTENT_ROOT = "/content/";
    private static final String DAM_ROOT = "/content/dam/";
    private static final String HTML_EXTENSION = ".html";
    private static final String FILE_REFERENCE = "fileReference";
    private static final String FEATURED_IMAGE = "cq:featuredimage";
    private static final String THUMBNAIL_IMAGE = "image";

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String path;

    @ValueMapValue
    private String ctaText;

    @ValueMapValue
    private String ctaLink;

    @SlingObject
    private ResourceResolver resourceResolver;

    private List<ArticleItem> articles;

    @PostConstruct
    protected void init() {
        articles = new ArrayList<>();
        if (isBlank(path)) {
            return;
        }
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page parent = pageManager == null ? null : pageManager.getPage(path.trim());
        if (parent == null) {
            return;
        }
        Iterator<Page> children = parent.listChildren();
        while (children.hasNext()) {
            Page child = children.next();
            if (child.isValid()) {
                articles.add(new ArticleItem(child, getThumbnail(child)));
            }
        }
    }

    /**
     * Gets the component title.
     * @return the title, or null if not set
     */
    public String getTitle() {
        return isBlank(title) ? null : title;
    }

    /**
     * Gets the child pages of the authored path as article cards.
     * @return an unmodifiable list of articles, never null
     */
    public List<ArticleItem> getArticles() {
        return Collections.unmodifiableList(articles);
    }

    /**
     * Gets the call-to-action label.
     * @return the CTA text, or null if not set
     */
    public String getCtaText() {
        return isBlank(ctaText) ? null : ctaText;
    }

    /**
     * Gets the call-to-action link. Page paths below {@code /content} get the {@code .html}
     * extension appended; assets and paths that already have an extension are returned unchanged.
     * @return the link, or null if not set
     */
    public String getCtaLink() {
        if (isBlank(ctaLink)) {
            return null;
        }
        String link = ctaLink.trim();
        if (link.startsWith(CONTENT_ROOT) && !link.startsWith(DAM_ROOT) && !hasExtension(link)) {
            return link + HTML_EXTENSION;
        }
        return link;
    }

    /**
     * Checks whether the call-to-action button can be rendered.
     * @return true if both CTA text and CTA link are set
     */
    public boolean hasCta() {
        return getCtaText() != null && getCtaLink() != null;
    }

    /**
     * Checks whether the component has anything to render.
     * @return true if the authored path has at least one child page
     */
    public boolean hasContent() {
        return !articles.isEmpty();
    }

    /**
     * Reads the page thumbnail: the Featured Image first, then the classic page thumbnail.
     */
    private static String getThumbnail(Page page) {
        Resource content = page.getContentResource();
        if (content == null) {
            return null;
        }
        String reference = getFileReference(content.getChild(FEATURED_IMAGE));
        return reference != null ? reference : getFileReference(content.getChild(THUMBNAIL_IMAGE));
    }

    private static String getFileReference(Resource image) {
        if (image == null) {
            return null;
        }
        String reference = image.getValueMap().get(FILE_REFERENCE, String.class);
        return isBlank(reference) ? null : reference;
    }

    private static boolean hasExtension(String link) {
        return link.substring(link.lastIndexOf('/') + 1).indexOf('.') >= 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
