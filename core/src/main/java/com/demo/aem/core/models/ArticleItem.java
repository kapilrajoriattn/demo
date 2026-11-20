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

import com.day.cq.wcm.api.Page;

/**
 * A single article card of the Recent Articles component, built from a child page.
 */
public class ArticleItem {

    private static final String HTML_EXTENSION = ".html";

    private final String url;
    private final String title;
    private final String description;
    private final String thumbnail;

    /**
     * Creates an article item.
     * @param page the child page this item represents
     * @param thumbnail the DAM path of the page thumbnail, may be null
     */
    public ArticleItem(Page page, String thumbnail) {
        this.url = page.getPath() + HTML_EXTENSION;
        this.title = firstNonBlank(page.getTitle(), page.getName());
        this.description = firstNonBlank(page.getDescription(), null);
        this.thumbnail = firstNonBlank(thumbnail, null);
    }

    /**
     * Gets the page URL.
     * @return the page path with the {@code .html} extension
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the page title, falling back to the page name.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the page description.
     * @return the description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the DAM path of the page thumbnail.
     * @return the thumbnail path, or null if the page has none
     */
    public String getThumbnail() {
        return thumbnail;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        return second;
    }
}
