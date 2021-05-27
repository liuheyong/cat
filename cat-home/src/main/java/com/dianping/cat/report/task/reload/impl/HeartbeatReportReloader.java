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
package com.dianping.cat.report.task.reload.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.task.reload.AbstractReportReloader;
import com.dianping.cat.report.task.reload.ReportReloadEntity;
import com.dianping.cat.report.task.reload.ReportReloader;

@Named(type = ReportReloader.class, value = HeartbeatAnalyzer.ID)
public class HeartbeatReportReloader extends AbstractReportReloader {

	@Inject(HeartbeatAnalyzer.ID)
	protected ReportManager<HeartbeatReport> m_reportManager;

	private List<HeartbeatReport> buildMergedReports(Map<String, List<HeartbeatReport>> mergedReports) {
		List<HeartbeatReport> results = new ArrayList<HeartbeatReport>();

		for (Entry<String, List<HeartbeatReport>> entry : mergedReports.entrySet()) {
			String domain = entry.getKey();
			HeartbeatReport report = new HeartbeatReport(domain);
			HeartbeatReportMerger merger = new HeartbeatReportMerger(report);

			report.setStartTime(report.getStartTime());
			report.setEndTime(report.getEndTime());

			for (HeartbeatReport r : entry.getValue()) {
				r.accept(merger);
			}
			results.add(merger.getHeartbeatReport());
		}

		return results;
	}

	@Override
	public String getId() {
		return HeartbeatAnalyzer.ID;
	}

	@Override
	public List<ReportReloadEntity> loadReport(long time) {
		List<ReportReloadEntity> results = new ArrayList<ReportReloadEntity>();
		Map<String, List<HeartbeatReport>> mergedReports = new HashMap<String, List<HeartbeatReport>>();

		for (int i = 0; i < getAnalyzerCount(); i++) {
			Map<String, HeartbeatReport> reports = m_reportManager.loadLocalReports(time, i);

			for (Entry<String, HeartbeatReport> entry : reports.entrySet()) {
				String domain = entry.getKey();
				HeartbeatReport r = entry.getValue();
				List<HeartbeatReport> rs = mergedReports.get(domain);

				if (rs == null) {
					rs = new ArrayList<HeartbeatReport>();

					mergedReports.put(domain, rs);
				}
				rs.add(r);
			}
		}

		List<HeartbeatReport> reports = buildMergedReports(mergedReports);

		for (HeartbeatReport r : reports) {
			HourlyReport report = new HourlyReport();

			report.setCreationDate(new Date());
			report.setDomain(r.getDomain());
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(getId());
			report.setPeriod(new Date(time));
			report.setType(1);

			byte[] content = DefaultNativeBuilder.build(r);
			ReportReloadEntity entity = new ReportReloadEntity(report, content);

			results.add(entity);
		}
		return results;
	}
}
