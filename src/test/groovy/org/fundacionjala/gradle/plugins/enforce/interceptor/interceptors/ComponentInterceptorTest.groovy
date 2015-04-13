/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.interceptor.interceptors

import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

class ComponentInterceptorTest extends Specification {
    @Shared
    String ROOT_PATH = System.properties['user.dir']

    @Shared
    String RESOURCE_PATH = "${ROOT_PATH}/src/test/groovy/org/fundacionjala/gradle/plugins/enforce/interceptor/resources"

    def "Should create gets components from source path"(){
        given:
            ComponentInterceptor componentInterceptor = new ComponentInterceptor()
            String path = Paths.get(RESOURCE_PATH, 'components').toString()
        when:
            componentInterceptor.loadFiles(path)
        then:
            componentInterceptor.files.size() == 1

    }
}
