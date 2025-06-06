/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hertzbeat.collector.collect;

import lombok.extern.slf4j.Slf4j;
import org.apache.hertzbeat.collector.dispatch.CollectDataDispatch;
import org.apache.hertzbeat.collector.dispatch.MetricsCollect;
import org.apache.hertzbeat.common.timer.Timeout;
import org.apache.hertzbeat.collector.timer.WheelTimerTask;
import org.apache.hertzbeat.common.constants.CommonConstants;
import org.apache.hertzbeat.common.entity.job.Job;
import org.apache.hertzbeat.common.entity.job.Metrics;
import org.apache.hertzbeat.common.entity.job.protocol.Protocol;
import org.apache.hertzbeat.common.entity.message.CollectRep;
import org.apache.hertzbeat.manager.dao.DefineDao;
import org.apache.hertzbeat.manager.service.impl.AppServiceImpl;
import org.apache.hertzbeat.manager.service.impl.ObjectStoreConfigServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AbstractCollectE2eTest
 */
@Slf4j
public abstract class AbstractCollectE2eTest {

    @InjectMocks
    protected AppServiceImpl appService;

    protected AbstractCollect collect;

    protected MetricsCollect metricsCollect;

    protected Metrics metrics;

    @Mock
    protected ObjectStoreConfigServiceImpl objectStoreConfigService;
    @Mock
    private WheelTimerTask timerJob;
    @Mock
    private Timeout timeout;
    @Mock
    private Job job;
    @Mock
    private DefineDao defineDao;

    public void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        when(defineDao.findAll()).thenReturn(new ArrayList<>());
        when(timeout.task()).thenReturn(timerJob);
        when(timerJob.getJob()).thenReturn(job);
        metricsCollect = new MetricsCollect(mock(Metrics.class), timeout, mock(CollectDataDispatch.class), null, List.of());

        // Initialize services and components
        appService.afterPropertiesSet();
        metrics = new Metrics();
    }

    /**
     * Validate metrics collection, check if the metrics values are not empty <br/>
     *
     * @param metricsDef metrics definition
     * @param metricName metric name
     * @return metrics data
     */
    protected CollectRep.MetricsData validateMetricsCollection(Metrics metricsDef, String metricName) {
        // By default, we do not allow empty values
        return validateMetricsCollection(metricsDef, metricName, false);
    }

    /**
     * Validate metrics collection, check if the metrics values are not empty <br/>
     * We believe that all monitoring metrics should have data
     *
     * @param metricsDef metrics definition
     * @param metricName metric name
     * @param allowEmpty In some special scenarios, it is not necessary to check if the value is `&nbsp;`
     */
    protected CollectRep.MetricsData validateMetricsCollection(Metrics metricsDef, String metricName, boolean allowEmpty) {
        CollectRep.MetricsData.Builder metricsData = collectMetrics(metricsDef);

        metricsCollect.calculateFields(metricsDef, metricsData);

        Assertions.assertTrue(metricsData.getValuesList().size() > 0,
                String.format("%s metrics values should not be empty, detail: %s", metricName, metricsData.getMsg()));

        for (CollectRep.ValueRow valueRow : metricsData.getValuesList()) {
            for (int i = 0; i < valueRow.getColumnsCount(); i++) {
                Assertions.assertFalse(valueRow.getColumns(i).isEmpty(),
                        String.format("%s metric column %d should not be empty", metricName, i));
                if (!allowEmpty) {
                    // Check if the value is not null
                    Assertions.assertNotEquals(CommonConstants.NULL_VALUE, valueRow.getColumns(i), String.format("%s metric column %d should not be null", metricName, i));
                }
            }
        }

        log.info("{} metrics validation passed", metricName);
        return metricsData.build();
    }

    /**
     * Set alias fields for metrics
     *
     * @param metrics    metrics
     * @param metricsDef metrics definition
     */
    protected void setMetricsAliasFields(Metrics metrics, Metrics metricsDef) {
        List<String> aliasFields = metricsDef.getAliasFields() == null
                ? metricsDef.getFields().stream().map(Metrics.Field::getField).collect(Collectors.toList())
                : metricsDef.getAliasFields();
        metrics.setAliasFields(aliasFields);
        metricsDef.setAliasFields(aliasFields);
    }

    protected abstract CollectRep.MetricsData.Builder collectMetrics(Metrics metricsDef);

    protected CollectRep.MetricsData.Builder collectMetricsData(Metrics metrics, Metrics metricsDef) {
        CollectRep.MetricsData.Builder metricsData = CollectRep.MetricsData.newBuilder();
        return this.collectMetricsData(metrics, metricsDef, metricsData);
    }

    protected CollectRep.MetricsData.Builder collectMetricsData(Metrics metrics, Metrics metricsDef, CollectRep.MetricsData.Builder metricsData) {
        setMetricsAliasFields(metrics, metricsDef);

        // Collect metrics
        collect.collect(metricsData, metrics);
        return metricsData;
    }

    protected abstract Protocol buildProtocol(Metrics metricsDef);
}
