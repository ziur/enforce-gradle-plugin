/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.undeploy

import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.MetadataComponents
import org.fundacionjala.gradle.plugins.enforce.wsc.rest.QueryBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Log
import org.fundacionjala.gradle.plugins.enforce.utils.Constants
import org.fundacionjala.gradle.plugins.enforce.utils.Util

/**
 * Validates files according salesForce json queries
 */
@Log
class SmartFilesValidator {

    ArrayList<String> jsonStringFormat
    ArrayList<Object> jsonQueries
    Map<String, ArrayList<String>> queryResult
    ArrayList<String> foldersSupported
    private final String METADATA_EXTENSION = '-meta.xml'
    private final int ZERO = 0

    /**
     * Initializes values
     * @param jsonStringFormat contains Json from salesForce in string format
     */
    public SmartFilesValidator(ArrayList<String> jsonStringFormat) {
        this.jsonStringFormat = jsonStringFormat
        jsonQueries = []
        foldersSupported = []
        queryResult = [:]
        convertJsonStringFormatToObject()
        fillMapOfFilesExistingOnOrganization()
        fillFoldersSupported()
    }

    /**
     * fills map of files according queries
     */
    private void fillMapOfFilesExistingOnOrganization() {
        ArrayList<String> files
        jsonQueries.each { json ->
            if (json.entityTypeName != null) {
                files = new ArrayList<String>()
                for (Object record in json.records) {
                    files.push(record.Name as String)
                }
                queryResult.put(json.entityTypeName as String, files)
            }
        }
    }

    /**
     * Uses JsonSluper to get json object from json string
     */
    private void convertJsonStringFormatToObject() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        jsonStringFormat.each { json ->
            jsonQueries.push(jsonSlurper.parseText(json as String))
        }
    }

    /**
     * Classifies files according queries like a filter
     * @param files contains all files to be evaluated
     * @return files selected
     */
    public filterFilesAccordingOrganization(ArrayList<File> files) {
        if (files == null) {
            throw new NullPointerException(String.format(Constants.NULL_PARAM_EXCEPTION, "files"))
        }

        ArrayList<File> filesClassified = []
        ArrayList<String> filesInOrganization = []
        ArrayList<String> invalidFolders = []
        files.each { file ->
            String folderName = file.getParentFile().getName()
            MetadataComponents component = MetadataComponents.getComponentByFolder(folderName as String)
            if (component) {
                filesInOrganization = queryResult.get(component.getTypeName())
                pushIfItIsPossible(file, folderName, filesClassified, filesInOrganization)
            } else {
                invalidFolders.add(folderName)
            }
        }
        if (!invalidFolders.isEmpty()) {
            Util.logList(log, Constants.UNSUPPORTED_FOLDERS, invalidFolders)
        }

        return filesClassified
    }

    /**
     * Fills files if they satisfies the requirements
     * @param file contains the file to evaluate
     * @param folderName contains folder name of file
     * @param filesClassified contains the files selected
     * @param filesInOrganization contains files in organization
     */
    private void pushIfItIsPossible(File file, String folderName, ArrayList<File> filesClassified, ArrayList<String> filesInOrganization) {
        String fileName = file.getName()
        if (isMetaXmlFile(file)) {
            fileName = file.getName().substring(ZERO, file.getName().indexOf(METADATA_EXTENSION))
        }
        if (!foldersSupported.contains(folderName)) {
            filesClassified.push(file)
            return
        }
        if (filesInOrganization != null && filesInOrganization.contains(Util.getFileName(fileName))) {
            filesClassified.push(file)
        }
    }

    /**
     * Verifies if file is an xml file
     * @param file contains the file to verifies
     * @return a boolean value
     */
    private boolean isMetaXmlFile(File file) {
        return file.getName().indexOf(METADATA_EXTENSION) != -1
    }

    /**
     * Fills all folder supported to create queries
     */
    private void fillFoldersSupported() {
        QueryBuilder.defaultComponents.each { typeFile ->
            foldersSupported.push(MetadataComponents.getDirectoryByName(typeFile))
        }
    }
}
