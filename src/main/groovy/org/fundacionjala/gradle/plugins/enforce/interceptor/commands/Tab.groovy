/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.interceptor.commands

import groovy.xml.StreamingMarkupBuilder
import org.fundacionjala.gradle.plugins.enforce.utils.Util

class Tab {
    private final String TAG_PAGES = "pages"
    private final String TAG_CUSTOM_OBJECT = "customObject"
    private final String XMLNS = "http://soap.sforce.com/2006/04/metadata"
    private final String DEACTIVATED = "false"
    private final String LABEL = "Custom53: Bell"
    private final String URL = "https://www.google.com"
    private final String ENCODING = "UTF-8"
    private final int FRAME = 600


    Closure execute = { file ->
        if (!file) return

        if (!file.text.contains(TAG_PAGES) && !file.text.contains(TAG_CUSTOM_OBJECT)) {
            def nameFile = Util.getFileName(file.getName())
            def contentWebTab = getWebTabString(nameFile)
            file.text = contentWebTab
        }
    }

    /**
     * Generate a basic web tab
     * @param nameFile is typeName file tab
     * @return xml web tab in format String
     */
    public String getWebTabString(String nameFile) {
        def builder = new StreamingMarkupBuilder()
        builder.encoding = ENCODING
        def forceXml = builder.bind {
            mkp.xmlDeclaration()
            CustomTab(xmlns: XMLNS) {
                frameHeight(FRAME)
                hasSidebar(DEACTIVATED)
                label(nameFile)
                mobileReady(DEACTIVATED)
                motif(LABEL)
                url(URL)
                urlEncodingKey(ENCODING)
            }
        }
        return forceXml.toString()
    }
}
