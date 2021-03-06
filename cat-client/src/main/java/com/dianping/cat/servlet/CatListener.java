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
package com.dianping.cat.servlet;

import com.dianping.cat.Cat;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * cat客户端启动入口
 *
 * @author: heyongliu
 * @date: 2022/4/15
 */
public class CatListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Cat.destroy();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String catClientXml = ctx.getInitParameter("cat-client-xml");

        if (catClientXml == null) {
            catClientXml = new File(Cat.getCatHome(), "client.xml").getPath();
        }
        Cat.initialize(new File(catClientXml));
    }
}
