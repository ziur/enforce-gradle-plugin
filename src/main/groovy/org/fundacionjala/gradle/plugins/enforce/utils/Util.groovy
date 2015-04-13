/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.utils

import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.MetadataComponents
import org.gradle.api.Project

import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.logging.Logger
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A set methods of utility
 */

class Util {
    private static final String PATTERN_EMAIL = '''^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)*[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$'''
    private static final String PATTERN_FILE_EXT = ~/[.][^.]+$/
    private static final int MAC_ADDRESS_SIZE = 12

    /**
     * Gets only file typeName without the extension
     * @param fileName the file typeName
     */
    public static String getFileName(String fileName) {
        fileName.replaceFirst(PATTERN_FILE_EXT, '')
    }

    /**
     * Verifies if the property exist and if it is not empty string
     * @param name the property typeName
     */
    public static boolean isValidProperty(Project project, String name) {
        project.hasProperty(name) && !project.properties[name].toString().equals("")
    }

    /**
     * Verify if the property is empty
     * @param name the property typeName
     */
    public static isEmptyProperty(Project project, String name) {
        project.hasProperty(name) && project.properties[name].toString().equals('')
    }
    /**
     * Validates required parameters that should be entered via command line
     * @param project The project reference
     * @param paramNames A string list with the parameter names
     * */
    public static Boolean validateRequiredParameters(Project project, List<String> paramNames) {
        def valid = true
        def projectRef = project
        paramNames.each { paramName ->
            if (!Util.isValidProperty(projectRef, paramName)) {
                println "Parameter -P${paramName} is required. "
                valid = false
            }
        }
        return valid
    }
    /**
     * Validates a email input
     * @param email is a string
     * @return true if email is valid else false
     */
    public static boolean validEmail(String email) {
        Pattern pattern = Pattern.compile(PATTERN_EMAIL)
        Matcher matcher = pattern.matcher(email)
        return matcher.find()
    }
    /**
     * Gets mac address of your computer
     * @return String mac address
     */
    private static String getMacAddress() {
        def macAddress
        def interfaces = NetworkInterface.networkInterfaces.collect { element ->
            element.hardwareAddress?.encodeHex().toString()
        }
        interfaces.each { mac ->
            if (mac && mac.size() == MAC_ADDRESS_SIZE) {
                macAddress = mac
            }
        }
        return macAddress
    }

    public static String formatDurationHMS(long milliseconds){

        long second = (milliseconds / 1000)
        second = second % 60
        long minute = (milliseconds / (1000 * 60))
        minute = minute % 60
        long hour = (milliseconds / (1000 * 60 * 60))
        hour = hour % 24
        milliseconds = milliseconds % 1000
        String time = String.format("%02d:%02d:%02d:%02d", hour, minute, second, milliseconds)
        return time
    }

    public static byte[] getBytes(String value, String charsetName){
        value.getBytes(Charset.forName(charsetName))
    }

    /**
     * Created a folder, if it is already created, it will be deleted that folder.
     * @param path contains the path of folder to create
     */
    public static void forceCreateFolder(String path) {
        File directory = new File(path)
        if (directory.exists()) {
            directory.deleteDir()
        }
        directory.mkdir()
    }

    /**
     * Logs an array list of strings
     * @param logger the instance Logger of java
     * @param folderNames an array list of strings
     */
    public static void logList(Logger logger, String message, ArrayList<String> stringArrayList){
        logger.info("${message}:\n")
        stringArrayList.each { folder ->
            logger.info(folder)
        }
    }

    /**
     * Gets extension file
     * @param file is type File
     * @return extension
     */
    public static String getFileExtension(File file) {
        String name = file.getName()
        int lastIndexOf = name.lastIndexOf(".")
        if (lastIndexOf == -1) {
            return "" // empty extension
        }
        return name.substring(lastIndexOf + 1)
    }

    /**
     * Gets folders invalid
     * @param foldersName are folders name
     * @return an Array list with invalid folders
     */
    public static ArrayList<String> getInvalidFolders (ArrayList<String> foldersName) {
        ArrayList<String> invalidFolders = new ArrayList<String>()
        foldersName.each {String folderName ->
            if (!MetadataComponents.validFolder(folderName)) {
                invalidFolders.push(folderName)
            }
        }
        return invalidFolders
    }

    /**
     * Gets folders empty
     * @param foldersName are folders name
     * @return an Array list with empty folders
     */
    public static ArrayList<String> getEmptyFolders (ArrayList<String> foldersName, String projectPath) {
        ArrayList<String> emptyFolders = new ArrayList<String>()
        foldersName.each {String folderName ->
            File file = new File(Paths.get(projectPath, folderName).toString())
            if (file.exists() && file.list().length == 0) {
                emptyFolders.push(folderName)
            }
        }
        return emptyFolders
    }

    /**
     * Gets folders name that don't exist
     * @param foldersName is type String
     * @param projectPath is type String
     * @return an Array of folders name that don't exist
     */
    public static ArrayList<String> getNotExistFolders(ArrayList<String> foldersName, String projectPath) {
        ArrayList<String> notExistFolders = new ArrayList<String>()
        foldersName.each {String folderName ->
            File file = new File(Paths.get(projectPath, folderName).toString())
            if (!file.exists()) {
                notExistFolders.push(folderName)
            }
        }
        return notExistFolders
    }
}
