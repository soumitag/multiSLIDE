import { coerceElement, coerceBooleanProperty, coerceNumberProperty } from '@angular/cdk/coercion';
import { Injectable, ɵɵdefineInjectable, ɵɵinject, EventEmitter, Directive, ElementRef, NgZone, Output, Input, NgModule } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
/**
 * Factory that creates a new MutationObserver and allows us to stub it out in unit tests.
 * @docs-private
 */
import * as ɵngcc0 from '@angular/core';
var MutationObserverFactory = /** @class */ (function () {
    function MutationObserverFactory() {
    }
    MutationObserverFactory.prototype.create = function (callback) {
        return typeof MutationObserver === 'undefined' ? null : new MutationObserver(callback);
    };
    MutationObserverFactory.ɵprov = ɵɵdefineInjectable({ factory: function MutationObserverFactory_Factory() { return new MutationObserverFactory(); }, token: MutationObserverFactory, providedIn: "root" });
MutationObserverFactory.ɵfac = function MutationObserverFactory_Factory(t) { return new (t || MutationObserverFactory)(); };
/*@__PURE__*/ (function () { ɵngcc0.ɵsetClassMetadata(MutationObserverFactory, [{
        type: Injectable,
        args: [{ providedIn: 'root' }]
    }], function () { return []; }, null); })();
    return MutationObserverFactory;
}());
/** An injectable service that allows watching elements for changes to their content. */
var ContentObserver = /** @class */ (function () {
    function ContentObserver(_mutationObserverFactory) {
        this._mutationObserverFactory = _mutationObserverFactory;
        /** Keeps track of the existing MutationObservers so they can be reused. */
        this._observedElements = new Map();
    }
    ContentObserver.prototype.ngOnDestroy = function () {
        var _this = this;
        this._observedElements.forEach(function (_, element) { return _this._cleanupObserver(element); });
    };
    ContentObserver.prototype.observe = function (elementOrRef) {
        var _this = this;
        var element = coerceElement(elementOrRef);
        return new Observable(function (observer) {
            var stream = _this._observeElement(element);
            var subscription = stream.subscribe(observer);
            return function () {
                subscription.unsubscribe();
                _this._unobserveElement(element);
            };
        });
    };
    /**
     * Observes the given element by using the existing MutationObserver if available, or creating a
     * new one if not.
     */
    ContentObserver.prototype._observeElement = function (element) {
        if (!this._observedElements.has(element)) {
            var stream_1 = new Subject();
            var observer = this._mutationObserverFactory.create(function (mutations) { return stream_1.next(mutations); });
            if (observer) {
                observer.observe(element, {
                    characterData: true,
                    childList: true,
                    subtree: true
                });
            }
            this._observedElements.set(element, { observer: observer, stream: stream_1, count: 1 });
        }
        else {
            this._observedElements.get(element).count++;
        }
        return this._observedElements.get(element).stream;
    };
    /**
     * Un-observes the given element and cleans up the underlying MutationObserver if nobody else is
     * observing this element.
     */
    ContentObserver.prototype._unobserveElement = function (element) {
        if (this._observedElements.has(element)) {
            this._observedElements.get(element).count--;
            if (!this._observedElements.get(element).count) {
                this._cleanupObserver(element);
            }
        }
    };
    /** Clean up the underlying MutationObserver for the specified element. */
    ContentObserver.prototype._cleanupObserver = function (element) {
        if (this._observedElements.has(element)) {
            var _a = this._observedElements.get(element), observer = _a.observer, stream = _a.stream;
            if (observer) {
                observer.disconnect();
            }
            stream.complete();
            this._observedElements.delete(element);
        }
    };
    /** @nocollapse */
    ContentObserver.ctorParameters = function () { return [
        { type: MutationObserverFactory }
    ]; };
    ContentObserver.ɵprov = ɵɵdefineInjectable({ factory: function ContentObserver_Factory() { return new ContentObserver(ɵɵinject(MutationObserverFactory)); }, token: ContentObserver, providedIn: "root" });
ContentObserver.ɵfac = function ContentObserver_Factory(t) { return new (t || ContentObserver)(ɵngcc0.ɵɵinject(MutationObserverFactory)); };
/*@__PURE__*/ (function () { ɵngcc0.ɵsetClassMetadata(ContentObserver, [{
        type: Injectable,
        args: [{ providedIn: 'root' }]
    }], function () { return [{ type: MutationObserverFactory }]; }, null); })();
    return ContentObserver;
}());
/**
 * Directive that triggers a callback whenever the content of
 * its associated element has changed.
 */
var CdkObserveContent = /** @class */ (function () {
    function CdkObserveContent(_contentObserver, _elementRef, _ngZone) {
        this._contentObserver = _contentObserver;
        this._elementRef = _elementRef;
        this._ngZone = _ngZone;
        /** Event emitted for each change in the element's content. */
        this.event = new EventEmitter();
        this._disabled = false;
        this._currentSubscription = null;
    }
    Object.defineProperty(CdkObserveContent.prototype, "disabled", {
        /**
         * Whether observing content is disabled. This option can be used
         * to disconnect the underlying MutationObserver until it is needed.
         */
        get: function () { return this._disabled; },
        set: function (value) {
            this._disabled = coerceBooleanProperty(value);
            this._disabled ? this._unsubscribe() : this._subscribe();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(CdkObserveContent.prototype, "debounce", {
        /** Debounce interval for emitting the changes. */
        get: function () { return this._debounce; },
        set: function (value) {
            this._debounce = coerceNumberProperty(value);
            this._subscribe();
        },
        enumerable: true,
        configurable: true
    });
    CdkObserveContent.prototype.ngAfterContentInit = function () {
        if (!this._currentSubscription && !this.disabled) {
            this._subscribe();
        }
    };
    CdkObserveContent.prototype.ngOnDestroy = function () {
        this._unsubscribe();
    };
    CdkObserveContent.prototype._subscribe = function () {
        var _this = this;
        this._unsubscribe();
        var stream = this._contentObserver.observe(this._elementRef);
        // TODO(mmalerba): We shouldn't be emitting on this @Output() outside the zone.
        // Consider brining it back inside the zone next time we're making breaking changes.
        // Bringing it back inside can cause things like infinite change detection loops and changed
        // after checked errors if people's code isn't handling it properly.
        this._ngZone.runOutsideAngular(function () {
            _this._currentSubscription =
                (_this.debounce ? stream.pipe(debounceTime(_this.debounce)) : stream).subscribe(_this.event);
        });
    };
    CdkObserveContent.prototype._unsubscribe = function () {
        if (this._currentSubscription) {
            this._currentSubscription.unsubscribe();
        }
    };
    /** @nocollapse */
    CdkObserveContent.ctorParameters = function () { return [
        { type: ContentObserver },
        { type: ElementRef },
        { type: NgZone }
    ]; };
    CdkObserveContent.propDecorators = {
        event: [{ type: Output, args: ['cdkObserveContent',] }],
        disabled: [{ type: Input, args: ['cdkObserveContentDisabled',] }],
        debounce: [{ type: Input }]
    };
CdkObserveContent.ɵfac = function CdkObserveContent_Factory(t) { return new (t || CdkObserveContent)(ɵngcc0.ɵɵdirectiveInject(ContentObserver), ɵngcc0.ɵɵdirectiveInject(ɵngcc0.ElementRef), ɵngcc0.ɵɵdirectiveInject(ɵngcc0.NgZone)); };
CdkObserveContent.ɵdir = ɵngcc0.ɵɵdefineDirective({ type: CdkObserveContent, selectors: [["", "cdkObserveContent", ""]], inputs: { disabled: ["cdkObserveContentDisabled", "disabled"], debounce: "debounce" }, outputs: { event: "cdkObserveContent" }, exportAs: ["cdkObserveContent"] });
/*@__PURE__*/ (function () { ɵngcc0.ɵsetClassMetadata(CdkObserveContent, [{
        type: Directive,
        args: [{
                selector: '[cdkObserveContent]',
                exportAs: 'cdkObserveContent'
            }]
    }], function () { return [{ type: ContentObserver }, { type: ɵngcc0.ElementRef }, { type: ɵngcc0.NgZone }]; }, { event: [{
            type: Output,
            args: ['cdkObserveContent']
        }], disabled: [{
            type: Input,
            args: ['cdkObserveContentDisabled']
        }], debounce: [{
            type: Input
        }] }); })();
    return CdkObserveContent;
}());
var ObserversModule = /** @class */ (function () {
    function ObserversModule() {
    }
ObserversModule.ɵmod = ɵngcc0.ɵɵdefineNgModule({ type: ObserversModule });
ObserversModule.ɵinj = ɵngcc0.ɵɵdefineInjector({ factory: function ObserversModule_Factory(t) { return new (t || ObserversModule)(); }, providers: [MutationObserverFactory] });
(function () { (typeof ngJitMode === "undefined" || ngJitMode) && ɵngcc0.ɵɵsetNgModuleScope(ObserversModule, { declarations: [CdkObserveContent], exports: [CdkObserveContent] }); })();
/*@__PURE__*/ (function () { ɵngcc0.ɵsetClassMetadata(ObserversModule, [{
        type: NgModule,
        args: [{
                exports: [CdkObserveContent],
                declarations: [CdkObserveContent],
                providers: [MutationObserverFactory]
            }]
    }], function () { return []; }, null); })();
    return ObserversModule;
}());

/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */

/**
 * Generated bundle index. Do not edit.
 */

export { CdkObserveContent, ContentObserver, MutationObserverFactory, ObserversModule };

//# sourceMappingURL=observers.js.map