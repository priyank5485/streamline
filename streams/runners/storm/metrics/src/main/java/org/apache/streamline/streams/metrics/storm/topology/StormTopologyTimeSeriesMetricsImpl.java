package org.apache.streamline.streams.metrics.storm.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streamline.streams.layout.TopologyLayoutConstants;
import org.apache.streamline.streams.layout.component.Component;
import org.apache.streamline.streams.layout.component.TopologyLayout;
import org.apache.streamline.streams.metrics.TimeSeriesQuerier;
import org.apache.streamline.streams.metrics.topology.TopologyTimeSeriesMetrics;
import org.apache.streamline.streams.storm.common.StormRestAPIClient;
import org.apache.streamline.streams.storm.common.StormTopologyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storm implementation of the TopologyTimeSeriesMetrics interface
 */
public class StormTopologyTimeSeriesMetricsImpl implements TopologyTimeSeriesMetrics {
    private final StormRestAPIClient client;
    private TimeSeriesQuerier timeSeriesQuerier;
    private final ObjectMapper mapper = new ObjectMapper();
    public static final StormMappedMetric[] STATS_METRICS = new StormMappedMetric[]{
            StormMappedMetric.inputRecords, StormMappedMetric.outputRecords, StormMappedMetric.ackedRecords,
            StormMappedMetric.failedRecords, StormMappedMetric.processedTime, StormMappedMetric.recordsInWaitQueue
    };

    public StormTopologyTimeSeriesMetricsImpl(StormRestAPIClient client) {
        this.client = client;
    }

    @Override
    public void setTimeSeriesQuerier(TimeSeriesQuerier timeSeriesQuerier) {
        this.timeSeriesQuerier = timeSeriesQuerier;
    }

    @Override
    public TimeSeriesQuerier getTimeSeriesQuerier() {
        return timeSeriesQuerier;
    }

    @Override
    public Map<Long, Double> getCompleteLatency(TopologyLayout topology, Component component, long from, long to) {
        assertTimeSeriesQuerierIsSet();

        String stormTopologyName = StormTopologyUtil.findOrGenerateTopologyName(client, topology.getId(), topology.getName());
        String stormComponentName = getComponentName(component);

        return queryComponentMetrics(stormTopologyName, stormComponentName, StormMappedMetric.completeLatency, from, to);
    }

    @Override
    public Map<String, Map<Long, Double>> getkafkaTopicOffsets(TopologyLayout topology, Component component, long from, long to) {
        assertTimeSeriesQuerierIsSet();

        String stormTopologyName = StormTopologyUtil.findOrGenerateTopologyName(client, topology.getId(), topology.getName());
        String stormComponentName = getComponentName(component);

        String topicName = findKafkaTopicName(topology, component);
        if (topicName == null) {
            throw new IllegalStateException("Cannot find Kafka topic name from source config - topology name: " +
                    topology.getName() + " / source : " + component.getName());
        }

        StormMappedMetric[] metrics = { StormMappedMetric.logsize, StormMappedMetric.offset, StormMappedMetric.lag };

        Map<String, Map<Long, Double>> kafkaOffsets = new HashMap<>();
        for (StormMappedMetric metric : metrics) {
            kafkaOffsets.put(metric.name(), queryKafkaMetrics(stormTopologyName, stormComponentName, metric, topicName, from, to));
        }

        return kafkaOffsets;
    }

    @Override
    public TimeSeriesComponentMetric getTopologyStats(TopologyLayout topology, long from, long to) {
        assertTimeSeriesQuerierIsSet();

        String stormTopologyName = StormTopologyUtil.findOrGenerateTopologyName(client, topology.getId(), topology.getName());

        Map<String, Map<Long, Double>> stats = new HashMap<>();
        Arrays.asList(STATS_METRICS).parallelStream()
                .forEach(m -> stats.put(m.name(), queryTopologyMetrics(stormTopologyName, m, from, to)));

        return buildTimeSeriesComponentMetric(topology.getName(), stats);
    }

    @Override
    public TimeSeriesComponentMetric getComponentStats(TopologyLayout topology, Component component, long from, long to) {
        assertTimeSeriesQuerierIsSet();

        String stormTopologyName = StormTopologyUtil.findOrGenerateTopologyName(client, topology.getId(), topology.getName());
        String stormComponentName = getComponentName(component);

        Map<String, Map<Long, Double>> componentStats = new ConcurrentHashMap<>();
        Arrays.asList(STATS_METRICS).parallelStream()
                .forEach(m -> componentStats.put(m.name(), queryComponentMetrics(stormTopologyName, stormComponentName, m, from, to)));

        return buildTimeSeriesComponentMetric(component.getName(), componentStats);
    }

    private TimeSeriesComponentMetric buildTimeSeriesComponentMetric(String name, Map<String, Map<Long, Double>> stats) {
        Map<String, Map<Long, Double>> misc = new HashMap<>();
        misc.put(StormMappedMetric.ackedRecords.name(), stats.get(StormMappedMetric.ackedRecords.name()));

        TimeSeriesComponentMetric metric = new TimeSeriesComponentMetric(name,
                stats.get(StormMappedMetric.inputRecords.name()),
                stats.get(StormMappedMetric.outputRecords.name()),
                stats.get(StormMappedMetric.failedRecords.name()),
                stats.get(StormMappedMetric.processedTime.name()),
                stats.get(StormMappedMetric.recordsInWaitQueue.name()),
                misc);

        return metric;
    }

    private void assertTimeSeriesQuerierIsSet() {
        if (timeSeriesQuerier == null) {
            throw new IllegalStateException("Time series querier is not set!");
        }
    }

    private String getComponentName(Component component) {
        return component.getId() + "-" + component.getName();
    }

    private String findKafkaTopicName(TopologyLayout topology, Component component) {
        String kafkaTopicName = null;
        try {
            Map<String, Object> topologyConfig = topology.getConfig().getProperties();
            List<Map<String, Object>> dataSources = (List<Map<String, Object>>) topologyConfig.get(TopologyLayoutConstants.JSON_KEY_DATA_SOURCES);

            for (Map<String, Object> dataSource : dataSources) {
                // UINAME and TYPE are mandatory fields for dataSource, so skip checking null
                String uiName = (String) dataSource.get(TopologyLayoutConstants.JSON_KEY_UINAME);
                String type = (String) dataSource.get(TopologyLayoutConstants.JSON_KEY_TYPE);

                if (!uiName.equals(component.getName())) {
                    continue;
                }

                if (!type.equalsIgnoreCase("KAFKA")) {
                    throw new IllegalStateException("Type of datasource should be KAFKA");
                }

                // config is a mandatory field for dataSource, so skip checking null
                Map<String, Object> dataSourceConfig = (Map<String, Object>) dataSource.get(TopologyLayoutConstants.JSON_KEY_CONFIG);
                kafkaTopicName = (String) dataSourceConfig.get(TopologyLayoutConstants.JSON_KEY_TOPIC);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse topology configuration.");
        }

        return kafkaTopicName;
    }

    private Map<Long, Double> queryTopologyMetrics(String stormTopologyName, StormMappedMetric mappedMetric, long from, long to) {
        Map<Long, Double> metrics = timeSeriesQuerier.getTopologyLevelMetrics(stormTopologyName,
                mappedMetric.getStormMetricName(), mappedMetric.getAggregateFunction(), from, to);
        return new TreeMap<>(metrics);
    }

    private Map<Long, Double> queryComponentMetrics(String stormTopologyName, String sourceId, StormMappedMetric mappedMetric, long from, long to) {
        Map<Long, Double> metrics = timeSeriesQuerier.getMetrics(stormTopologyName, sourceId, mappedMetric.getStormMetricName(),
                mappedMetric.getAggregateFunction(), from, to);
        return new TreeMap<>(metrics);
    }

    private Map<Long, Double> queryKafkaMetrics(String stormTopologyName, String sourceId, StormMappedMetric mappedMetric,
                                                  String kafkaTopic, long from, long to) {
        Map<Long, Double> metrics = timeSeriesQuerier.getMetrics(stormTopologyName, sourceId, String.format(mappedMetric.getStormMetricName(), kafkaTopic),
                mappedMetric.getAggregateFunction(), from, to);
        return new TreeMap<>(metrics);
    }

}
