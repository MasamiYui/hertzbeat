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

package org.apache.hertzbeat.alert.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AlertManagerExternAlert
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertManagerExternAlert {
    
    private String groupKey;
    
    private String externalURL;
    
    private String version;
    
    private String status;
    
    private Map<String, String> groupLabels;
    
    private Map<String, String> commonLabels;
    
    private Map<String, String> commonAnnotations;
    
    private List<PrometheusExternAlert> alerts;
}
