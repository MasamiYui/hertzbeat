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

package org.apache.hertzbeat.collector.collect.ssh;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.hertzbeat.collector.dispatch.DispatchConstants;
import org.apache.hertzbeat.common.entity.job.Metrics;
import org.apache.hertzbeat.common.entity.job.protocol.SshProtocol;
import org.apache.hertzbeat.common.entity.message.CollectRep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SshCollectImpl}
 */
class SshCollectImplTest {
    private SshCollectImpl sshCollect;
    private CollectRep.MetricsData.Builder builder;

    @BeforeEach
    void setUp() {
        sshCollect = new SshCollectImpl();
        builder = CollectRep.MetricsData.newBuilder();
    }

    @Test
    void preCheck() {
        // metrics is null
        assertThrows(IllegalArgumentException.class, () -> {
            sshCollect.preCheck(null);
        });

        // ssh protocol is null
        assertThrows(IllegalArgumentException.class, () -> {
            Metrics metrics = Metrics.builder().build();
            sshCollect.preCheck(metrics);
        });

        // everything is ok
        assertDoesNotThrow(() -> {
            Metrics metrics = Metrics.builder().ssh(new SshProtocol()).build();
            sshCollect.preCheck(metrics);
        });
    }

    @Test
    void collect() {
        assertDoesNotThrow(() -> {
            Metrics metrics = Metrics.builder().ssh(new SshProtocol()).build();
            sshCollect.collect(builder, metrics);
            assertEquals(CollectRep.Code.FAIL, builder.getCode());
        });
    }

    @Test
    void supportProtocol() {
        assertEquals(DispatchConstants.PROTOCOL_SSH, sshCollect.supportProtocol());
    }
}
