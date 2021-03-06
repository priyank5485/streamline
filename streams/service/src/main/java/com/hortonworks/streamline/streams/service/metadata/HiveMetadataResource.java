/**
  * Copyright 2017 Hortonworks.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at

  *   http://www.apache.org/licenses/LICENSE-2.0

  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 **/
package com.hortonworks.streamline.streams.service.metadata;

import com.codahale.metrics.annotation.Timed;
import com.hortonworks.streamline.common.util.WSUtils;
import com.hortonworks.streamline.streams.catalog.Cluster;
import com.hortonworks.streamline.streams.catalog.service.EnvironmentService;
import com.hortonworks.streamline.streams.catalog.service.StreamCatalogService;

import com.hortonworks.streamline.streams.catalog.exception.EntityNotFoundException;
import com.hortonworks.streamline.streams.catalog.service.metadata.HiveMetadataService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/v1/catalog")
@Produces(MediaType.APPLICATION_JSON)
public class HiveMetadataResource {
    private final EnvironmentService environmentService;

    public HiveMetadataResource(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GET
    @Path("/clusters/{clusterId}/services/hive/databases")
    @Timed
    public Response getDatabasesByClusterId(@PathParam("clusterId") Long clusterId)
        throws Exception {
        try(final HiveMetadataService hiveMetadataService = HiveMetadataService.newInstance(environmentService, clusterId)) {
            return WSUtils.respondEntity(hiveMetadataService.getHiveDatabases(), OK);
        } catch (EntityNotFoundException ex) {
            throw com.hortonworks.streamline.common.exception.service.exception.request.EntityNotFoundException.byId(ex.getMessage());
        }
    }

    @GET
    @Path("/clusters/{clusterId}/services/hive/databases/{dbName}/tables")
    @Timed
    public Response getDatabaseTablesByClusterId(@PathParam("clusterId") Long clusterId, @PathParam("dbName") String dbName)
        throws Exception {
        try(final HiveMetadataService hiveMetadataService = HiveMetadataService.newInstance(environmentService, clusterId)) {
            return WSUtils.respondEntity(hiveMetadataService.getHiveTables(dbName), OK);
        } catch (EntityNotFoundException ex) {
            throw com.hortonworks.streamline.common.exception.service.exception.request.EntityNotFoundException.byId(ex.getMessage());
        }
    }
}
