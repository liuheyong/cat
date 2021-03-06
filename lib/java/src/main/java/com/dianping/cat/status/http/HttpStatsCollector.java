/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.status.http;

import com.dianping.cat.status.AbstractCollector;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpStatsCollector extends AbstractCollector {

    private Map<String, Number> doClassLoadingCollect() {
        Map<String, Number> map = new LinkedHashMap<>();
        HttpStats stats = HttpStats.getAndReset();
        map.put("http.count", stats.getHttpCount());
        map.put("http.meantime", stats.getHttpMeantime());
        map.put("http.status400.count", stats.getHttpStatus400Count());
        map.put("http.status500.count", stats.getHttpStatus500Count());
        return map;
    }

    @Override
    public String getId() {
        return "http.status";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, Number> map = doClassLoadingCollect();
        return convert(map);
    }
}
