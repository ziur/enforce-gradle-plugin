/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.wsc

import com.sforce.soap.metadata.DeployResult
import com.sforce.soap.metadata.MetadataConnection
import com.sforce.soap.metadata.RetrieveResult
import org.fundacionjala.gradle.plugins.enforce.utils.Constants
import org.fundacionjala.gradle.plugins.enforce.utils.Util

import java.nio.charset.Charset

/**
 * Contains the logic which wait results from sales force server
 */
class InspectorResults {
    MetadataConnection metadataConnection
    private final int END_VAL_PROGRESS_BAR = 50
    private final int HUNDRED = 100
    private final String TIMEOUT_EXCEPTION = "Request timed out."
    OutputStream outputStream

    InspectorResults(MetadataConnection metadataConnection, OutputStream outputStream) {
        this.metadataConnection = metadataConnection
        this.outputStream = outputStream
    }

    /**
     * Waits for the deploy to complete
     * @param metadataConnection the metadata connection
     * @param asyncResultId the ID that returns the metadata connection
     * @param maxPolls the maximum number of attempts to deploy
     * @param millisPerPoll the milliseconds number per poll
     * @throws Exception if an request timed out or could not retrieve the metadata
     */
    public DeployResult waitForDeployResult(String asyncResultId, int maxPolls, int waitTimeMilliSecs) throws Exception {
       int poll = Constants.ZERO
        DeployResult deployResult = metadataConnection.checkDeployStatus(asyncResultId, true)
        int percent = Constants.ZERO
        percent = getDeployPercentage(deployResult)
        writePercent(percent)
        while (!deployResult.isDone()) {
            if (poll++ > maxPolls) {
                throw new Exception(TIMEOUT_EXCEPTION)
            }
            Thread.sleep(waitTimeMilliSecs)
            deployResult = metadataConnection.checkDeployStatus(asyncResultId, true)
            percent = getDeployPercentage(deployResult)
            writePercent(percent)
            if (percent == HUNDRED && deployResult.done) {
                break
            }
        }
        outputStream.write(Util.getBytes("\r\n", "UTF-8"))
        outputStream.flush()
        return deployResult
    }

    /**
     * Waits for the retrieve to complete
     * @param metadataConnection the metadata connection
     * @param asyncResultId the ID that returns the metadata connection
     * @param maxPolls the maximum number of attempts to deploy
     * @param millisPerPoll the milliseconds number per poll
     * @throws Exception if an request timed out or could not retrieve the metadata
     */
    public RetrieveResult waitForRetrieveResult(String asyncResultId, int maxPolls, int waitTimeMilliSecs) throws Exception {
        int poll = Constants.ZERO
        RetrieveResult result = metadataConnection.checkRetrieveStatus(asyncResultId)
        while (!result.isDone()) {
            Thread.sleep(waitTimeMilliSecs)
            if (poll++ > maxPolls) {
                throw new Exception(TIMEOUT_EXCEPTION)
            }
            result = metadataConnection.checkRetrieveStatus(asyncResultId)
        }
        return result
    }

    public int getDeployPercentage(DeployResult deployResult) {
        float percent = deployResult.numberComponentsTotal && deployResult.numberComponentsTotal > Constants.ZERO ?
                (deployResult.numberComponentsDeployed / deployResult.numberComponentsTotal) * HUNDRED : Constants.ZERO
        return Math.round(percent)
    }

    public void writePercent(int percent) {
        StringBuilder progressBar = new StringBuilder("[")

        for (int i = Constants.ZERO; i < END_VAL_PROGRESS_BAR; i++) {
            if (i < (percent / 2)) {
                progressBar.append("=")
            } else {
                progressBar.append(" ")
            }
        }
        progressBar.append("]   " + percent + "%     ");
        outputStream.write("\r${progressBar.toString()}".getBytes(Charset.forName("UTF-8")))
        outputStream.flush()
    }
}
