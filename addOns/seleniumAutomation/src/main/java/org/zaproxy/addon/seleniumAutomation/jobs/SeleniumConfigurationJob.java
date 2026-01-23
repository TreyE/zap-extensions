/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.seleniumAutomation.jobs;

import java.util.LinkedHashMap;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.parosproxy.paros.control.Control;
import org.zaproxy.addon.automation.AutomationEnvironment;
import org.zaproxy.addon.automation.AutomationJob;
import org.zaproxy.addon.automation.AutomationProgress;
import org.zaproxy.addon.automation.jobs.JobUtils;
import org.zaproxy.addon.seleniumAutomation.ExtensionSeleniumAutomation;
import org.zaproxy.zap.extension.selenium.ExtensionSelenium;

public class SeleniumConfigurationJob extends AutomationJob {
    private ExtensionSelenium extSelenium;
    private WebDriver wd;

    private SeleniumConfigurationJobParameters parameters;
    private SeleniumConfigurationJobData data;

    public SeleniumConfigurationJob() {
        this.parameters = new SeleniumConfigurationJobParameters();
        this.data = new SeleniumConfigurationJobData(this, parameters);
    }

    @Override
    public void runJob(AutomationEnvironment env, AutomationProgress progress) {}

    @Override
    public String getType() {
        return "selenium-configuration";
    }

    @Override
    public Order getOrder() {
        return Order.CONFIGS;
    }

    @Override
    public Object getParamMethodObject() {
        return null;
    }

    @Override
    public String getParamMethodName() {
        return null;
    }

    @Override
    public void planFinished() {
        wd.quit();
    }

    @Override
    public void planStarted() {
        extSelenium = getExtSelenium();
                
        wd = extSelenium.getProxiedBrowserByName(this.getParameters().getBrowser());
        wd.get(this.getParameters().getStartUrl());
    }
    
    public ExtensionSeleniumAutomation getExtSeleniumAutomation() {
        return Control.getSingleton().getExtensionLoader().getExtension(ExtensionSeleniumAutomation.class);
    }
    
    public ExtensionSelenium getExtSelenium() {
        return Control.getSingleton().getExtensionLoader().getExtension(ExtensionSelenium.class);
    }

    @Override
    public SeleniumConfigurationJobData getData() {
        return data;
    }

    @Override
    public SeleniumConfigurationJobParameters getParameters() {
        return parameters;
    }

    @Override
    public void showDialog() {
        new SeleniumConfigurationJobDialog(this).setVisible(true);
    }
    
    @Override
    public void verifyParameters(AutomationProgress progress) {
        Map<?, ?> jobData = this.getJobData();
        if (jobData != null) {
            JobUtils.applyParamsToObject(
                    (LinkedHashMap<?, ?>) jobData.get("parameters"),
                    this.parameters,
                    this.getName(),
                    null,
                    progress);
        }
    }
}
