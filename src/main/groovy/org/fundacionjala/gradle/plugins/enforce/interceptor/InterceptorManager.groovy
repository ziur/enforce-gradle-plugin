/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */



package org.fundacionjala.gradle.plugins.enforce.interceptor

import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.MetadataComponents

/**
 * This class manages all components created from source path
 */
class InterceptorManager {

    Map<String, MetadataInterceptor> interceptors
    List truncatedDirectories = ['classes', 'objects', 'triggers', 'pages', 'components', 'workflows', 'tabs']
    List<String> interceptorsToExecute
    /**
     * Creates a new interceptor management from source path
     * @param sourcePath the source directory path
     */
    InterceptorManager() {
        this.interceptors = [:]
        interceptorsToExecute = []
    }

    /**
     * Builds all interceptors from source path
     */
    public void buildInterceptors() {
        truncatedDirectories.each { dir ->
            MetadataComponents componentType = MetadataComponents.getComponentByFolder(dir as String)
            FactoryInterceptor factoryComponent = new FactoryInterceptor()
            MetadataInterceptor interceptor = factoryComponent.getInterceptor(componentType)
            if (interceptor) {
                interceptor.loadInterceptors()
                this.interceptors.put(componentType.directory, interceptor)
            }
        }
    }

    /**
     * Adds new interceptors from a map
     * @param interceptorsRegistered All interceptors registered
     */
    public void addInterceptorsRegistered(Map<String, Map<String, Closure>> interceptorsRegistered) {
        interceptorsRegistered.each { componentName, interceptors ->
            interceptors.each { cmdName, interceptor ->
                addInterceptor(componentName, cmdName, interceptor)
            }
        }
    }

    /**
     * Loads all files from a source path for each salesforce directory valid
     * @param sourcePath the source path to truncate
     */
    public void loadFiles(String sourcePath) {
        this.interceptors.values().each { interceptor ->
            interceptor.loadFiles(sourcePath)
        }
    }

    /**
     * Adds a list interceptor names to execute
     * @param interceptors the list interceptor names
     */
    public void addInterceptors(List<String> interceptors) {
        interceptorsToExecute = interceptors
        this.interceptors.values().each { interceptor ->
            interceptor.interceptorsToExecute = interceptors
        }
    }

    /**
     * Validates if all interceptors to execute were added
     */
    public void validateInterceptors(){
        List<String> interceptorNames = []
        for (cmdName in interceptorsToExecute){
            boolean found = false
            for (interceptor in this.interceptors.values()){
                if(interceptor.interceptors.containsKey(cmdName)){
                    found = true
                    break
                }
            }
            if(!found){
                interceptorNames.add(cmdName)
            }
        }
        if(interceptorNames.size() > 0){
            String message = interceptorNames.size() == 1?"The ${interceptorNames.pop()} interceptor was not found":
                                                  "The ${interceptorNames.join(',')} interceptors were not found"
            throw new Exception(message)
        }
    }

    /**
     * Executes the truncate method of all the component interceptors
     */
    public void executeTruncate() {
        this.interceptors.values().each { interceptor ->
            interceptor.executeInterceptors()
        }
    }

    /**
     * Adds new interceptor for a specific metadata group
     * @param metadataComponent the metadata group name
     * @param interceptorName the interceptor name
     * @param interceptorAction the new interceptor
     */
    void addInterceptor(String metadataComponent, String interceptorName, Closure interceptorAction) {
        MetadataInterceptor interceptor = this.interceptors.get(metadataComponent)
        if (interceptor) {
            interceptorName = interceptorName ?: interceptorAction.hashCode().toString()
            interceptor.addInterceptor(interceptorName, interceptorAction)
        }
    }

    /**
     * Adds new interceptor in the first position for a specific metadata group
     * @param metadataComponent the metadata group name
     * @param interceptorName the interceptor name
     * @param interceptorAction the new interceptor
     */
    void addFirstInterceptor(String metadataComponent, String interceptorName, Closure interceptorAction) {
        MetadataInterceptor interceptor = this.interceptors.get(metadataComponent)
        if (interceptor) {
            interceptorName = interceptorName ?: interceptorAction.hashCode().toString()
            interceptor.addInterceptor(0, interceptorName, interceptorAction)
        }
    }
}
