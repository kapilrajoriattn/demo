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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model for a single slide of the Custom Carousel component.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SlideItem {

    private static final String CONTENT_ROOT = "/content/";
    private static final String DAM_ROOT = "/content/dam/";
    private static final String HTML_EXTENSION = ".html";

    @ValueMapValue
    private String image;

    @ValueMapValue
    private String heading;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private String ctaText;

    @ValueMapValue
    private String ctaLink;

    /**
     * Gets the DAM path of the slide image.
     * @return the image path, or null if not set
     */
    public String getImage() {
        return isBlank(image) ? null : image;
    }

    /**
     * Gets the slide heading (rich text).
     * @return the heading markup, or null if not set
     */
    public String getHeading() {
        return isBlank(heading) ? null : heading;
    }

    /**
     * Gets the slide description (rich text).
     * @return the description markup, or null if not set
     */
    public String getDescription() {
        return isBlank(description) ? null : description;
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
     * Checks whether the slide has anything to render.
     * @return true if at least one authored value is set
     */
    public boolean hasContent() {
        return getImage() != null || getHeading() != null || getDescription() != null || hasCta();
    }

    private static boolean hasExtension(String path) {
        return path.substring(path.lastIndexOf('/') + 1).indexOf('.') >= 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
