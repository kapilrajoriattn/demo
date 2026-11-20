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
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;

/**
 * Sling Model for the Custom Carousel component.
 * Provides the authored slides for rendering.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CustomCarouselModel {

    @ChildResource(name = "slides")
    private List<Resource> slideResources;

    private List<SlideItem> slides;

    @PostConstruct
    protected void init() {
        slides = new ArrayList<>();
        if (slideResources != null) {
            for (Resource slideResource : slideResources) {
                SlideItem slide = slideResource.adaptTo(SlideItem.class);
                if (slide != null && slide.hasContent()) {
                    slides.add(slide);
                }
            }
        }
    }

    /**
     * Gets the slides that have content.
     * @return an unmodifiable list of slides, never null
     */
    public List<SlideItem> getSlides() {
        return Collections.unmodifiableList(slides);
    }

    /**
     * Checks whether the carousel has more than one slide, i.e. whether navigation is needed.
     * @return true if there are at least two slides
     */
    public boolean isNavigable() {
        return slides.size() > 1;
    }

    /**
     * Checks whether the carousel has anything to render.
     * @return true if at least one slide has content
     */
    public boolean hasContent() {
        return !slides.isEmpty();
    }
}
