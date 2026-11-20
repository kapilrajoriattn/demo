(function () {
    'use strict';

    var IS = 'custom-carousel';
    var SELECTOR_SELF = '[data-cmp-is="' + IS + '"]';
    var HOOK = 'data-cmp-hook-' + IS;
    var ACTIVE_ITEM = 'cmp-custom-carousel__item--active';
    var ACTIVE_INDICATOR = 'cmp-custom-carousel__indicator--active';

    function CustomCarousel(element) {
        var that = this;
        this.element = element;
        this.items = Array.prototype.slice.call(element.querySelectorAll('.cmp-custom-carousel__item'));
        this.indicators = Array.prototype.slice.call(element.querySelectorAll('[' + HOOK + '="indicator"]'));
        this.previous = element.querySelector('[' + HOOK + '="previous"]');
        this.next = element.querySelector('[' + HOOK + '="next"]');
        this.active = 0;

        if (this.previous) {
            this.previous.addEventListener('click', function () {
                that.navigate(that.active - 1);
            });
        }
        if (this.next) {
            this.next.addEventListener('click', function () {
                that.navigate(that.active + 1);
            });
        }
        this.indicators.forEach(function (indicator, index) {
            indicator.addEventListener('click', function () {
                that.navigate(index);
            });
        });
        element.addEventListener('keydown', function (event) {
            that.onKeyDown(event);
        });

        this.show(0);
    }

    /**
     * Shows the slide at the given index, wrapping around at both ends.
     * @param {number} index target slide index
     * @returns {number} the index that was actually shown
     */
    CustomCarousel.prototype.show = function (index) {
        var count = this.items.length;
        if (count === 0) {
            return -1;
        }
        var target = ((index % count) + count) % count;

        this.items.forEach(function (item, i) {
            var isActive = i === target;
            item.classList.toggle(ACTIVE_ITEM, isActive);
            item.hidden = !isActive;
        });
        this.indicators.forEach(function (indicator, i) {
            var isActive = i === target;
            indicator.classList.toggle(ACTIVE_INDICATOR, isActive);
            indicator.setAttribute('aria-selected', isActive ? 'true' : 'false');
            indicator.setAttribute('tabindex', isActive ? '0' : '-1');
        });
        this.active = target;
        return target;
    };

    CustomCarousel.prototype.navigate = function (index, moveFocusToIndicator) {
        var target = this.show(index);
        if (moveFocusToIndicator && this.indicators[target]) {
            this.indicators[target].focus();
        }
    };

    CustomCarousel.prototype.onKeyDown = function (event) {
        var target = event.target;
        if (event.altKey || event.ctrlKey || event.metaKey || event.shiftKey) {
            return;
        }
        if (target && (target.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName))) {
            return;
        }

        var focusOnIndicator = this.indicators.indexOf(target) !== -1;
        var key = event.key;
        switch (key) {
            case 'ArrowLeft':
            case 'Left':
                this.navigate(this.active - 1, focusOnIndicator);
                break;
            case 'ArrowRight':
            case 'Right':
                this.navigate(this.active + 1, focusOnIndicator);
                break;
            case 'Home':
                this.navigate(0, focusOnIndicator);
                break;
            case 'End':
                this.navigate(this.items.length - 1, focusOnIndicator);
                break;
            default:
                return;
        }
        event.preventDefault();
    };

    function init(root) {
        var elements = (root || document).querySelectorAll(SELECTOR_SELF);
        Array.prototype.forEach.call(elements, function (element) {
            if (!element.hasAttribute('data-cmp-initialized')) {
                element.setAttribute('data-cmp-initialized', 'true');
                new CustomCarousel(element);
            }
        });
    }

    function onDocumentReady() {
        init(document);

        // Re-initialise carousels added later, e.g. when the page editor refreshes the component
        var body = document.querySelector('body');
        if (body && 'MutationObserver' in window) {
            new MutationObserver(function (mutations) {
                mutations.forEach(function (mutation) {
                    Array.prototype.forEach.call(mutation.addedNodes, function (node) {
                        if (node.nodeType === 1) {
                            if (node.matches && node.matches(SELECTOR_SELF)) {
                                init(node.parentNode);
                            } else if (node.querySelector && node.querySelector(SELECTOR_SELF)) {
                                init(node);
                            }
                        }
                    });
                });
            }).observe(body, { subtree: true, childList: true });
        }
    }

    if (document.readyState !== 'loading') {
        onDocumentReady();
    } else {
        document.addEventListener('DOMContentLoaded', onDocumentReady);
    }
}());
