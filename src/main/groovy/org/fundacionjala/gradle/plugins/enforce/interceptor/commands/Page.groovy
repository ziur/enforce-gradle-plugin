/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.interceptor.commands

import java.util.regex.Matcher

/**
 * Implements the truncated algorithm to truncate the page content
 */
class Page {
    private final String REGEX_PAGE = /([cC]ontroller|standard[cC]ontroller)\s*=\s*(\"\w*\")/
    private final String STANDARD_CONTROLLER_NAME = 'standardcontroller'
    private final int MATCHER_INDEX = 0
    private final int CONTROLLER_INDEX = 1
    private final int CLASS_NAME_INDEX = 2
    public static final String EMPTY_PAGE = '<apex:page > </apex:page>'
    private String PAGE_WITH_CONTROLLER = "<apex:page %s = %s > </apex:page>"

    /**
     * A closure to truncate the page content
     */
    Closure execute = { file ->
        if (!file) return
        String truncatedCode = EMPTY_PAGE
        Matcher matcher = file.text =~ REGEX_PAGE
        if (matcher) {
            String controller = matcher[MATCHER_INDEX][CONTROLLER_INDEX]
            String className = matcher[MATCHER_INDEX][CLASS_NAME_INDEX]
            if (controller.toLowerCase().trim() == STANDARD_CONTROLLER_NAME) {
                truncatedCode = String.format(PAGE_WITH_CONTROLLER, controller, className)
            }
        }
        file.text = truncatedCode
    }
}
