/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hortonworks.iotas.webservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class IotasConfiguration extends Configuration {

    @NotEmpty
    private String topologyActionsImpl;

    @NotEmpty
    private String topologyMetricsImpl;

    @NotEmpty
    private String iotasStormJar;

    private Boolean notificationsRestDisable;

    private String javaJarCommand;

    @NotEmpty
    private String catalogRootUrl;

    @NotNull
    private FileStorageConfiguration fileStorageConfiguration;

    @NotEmpty
    private String stormHomeDir;

    @NotNull
    private StorageProviderConfiguration storageProviderConfiguration;

    @JsonProperty
    public StorageProviderConfiguration getStorageProviderConfiguration() {
        return storageProviderConfiguration;
    }

    @JsonProperty
    public void setStorageProviderConfiguration(StorageProviderConfiguration storageProviderConfiguration) {
        this.storageProviderConfiguration = storageProviderConfiguration;
    }

    @NotEmpty
    private String stormApiRootUrl;

    private String customProcessorWatchPath;

    private String customProcessorUploadFailPath;

    private String customProcessorUploadSuccessPath;

    private TimeSeriesDBConfiguration timeSeriesDBConfiguration;

    public String getTopologyActionsImpl() {
        return topologyActionsImpl;
    }

    public void setTopologyActionsImpl(String topologyActionsImpl) {
        this.topologyActionsImpl = topologyActionsImpl;
    }

    public String getIotasStormJar () {
        return iotasStormJar;
    }

    public void setIotasStormJar (String iotasStormJar) {
        this.iotasStormJar = iotasStormJar;
    }

    public String getCatalogRootUrl () {
        return catalogRootUrl;
    }

    public void setCatalogRootUrl (String catalogRootUrl) {
        this.catalogRootUrl = catalogRootUrl;
    }

    public String getJavaJarCommand() {
        return javaJarCommand;
    }

    public void setJavaJarCommand(String javaJarCommand) {
        this.javaJarCommand = javaJarCommand;
    }

    public FileStorageConfiguration getFileStorageConfiguration() {
        return this.fileStorageConfiguration;
    }

    public void setFileStorageConfiguration(FileStorageConfiguration configuration) {
        this.fileStorageConfiguration = configuration;
    }

    @JsonProperty("notificationsRestDisable")
    public Boolean isNotificationsRestDisabled() {
        return notificationsRestDisable != null ? notificationsRestDisable : false;
    }

    @JsonProperty("notificationsRestDisable")
    public void setNotificationsRestDisabled(Boolean notificationsRestDisable) {
        this.notificationsRestDisable = notificationsRestDisable;
    }

    public String getStormHomeDir () {
        return stormHomeDir;
    }

    public void setStormHomeDir (String stormHomeDir) {
        this.stormHomeDir = stormHomeDir;
    }

    public String getCustomProcessorWatchPath () {
        return customProcessorWatchPath;
    }

    public void setCustomProcessorWatchPath (String customProcessorWatchPath) {
        this.customProcessorWatchPath = customProcessorWatchPath;
    }

    public String getCustomProcessorUploadFailPath () {
        return customProcessorUploadFailPath;
    }

    public void setCustomProcessorUploadFailPath (String customProcessorUploadFailPath) {
        this.customProcessorUploadFailPath = customProcessorUploadFailPath;
    }

    public String getCustomProcessorUploadSuccessPath () {
        return customProcessorUploadSuccessPath;
    }

    public void setCustomProcessorUploadSuccessPath (String customProcessorUploadSuccessPath) {
        this.customProcessorUploadSuccessPath = customProcessorUploadSuccessPath;
    }

    public String getTopologyMetricsImpl() {
        return topologyMetricsImpl;
    }

    public void setTopologyMetricsImpl(String topologyMetricsImpl) {
        this.topologyMetricsImpl = topologyMetricsImpl;
    }

    public String getStormApiRootUrl() {
        return stormApiRootUrl;
    }

    public void setStormApiRootUrl(String stormApiRootUrl) {
        this.stormApiRootUrl = stormApiRootUrl;
    }

    public TimeSeriesDBConfiguration getTimeSeriesDBConfiguration() {
        return timeSeriesDBConfiguration;
    }

    public void setTimeSeriesDBConfiguration(TimeSeriesDBConfiguration timeSeriesDBConfiguration) {
        this.timeSeriesDBConfiguration = timeSeriesDBConfiguration;
    }
}
