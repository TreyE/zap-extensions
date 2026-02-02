/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.addon.automationEnhancements.jobs;

import java.util.LinkedHashMap;
import java.util.Map;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.zaproxy.addon.automation.AutomationEnvironment;
import org.zaproxy.addon.automation.AutomationJob;
import org.zaproxy.addon.automation.AutomationProgress;
import org.zaproxy.addon.automation.jobs.JobUtils;
import org.zaproxy.addon.automationEnhancements.ExtensionAutomationEnhancements;
import org.zaproxy.addon.automationEnhancements.jobs.ui.SessionCleanupJobDialog;
import org.zaproxy.zap.extension.alert.ExtensionAlert;

public class SessionCleanupJob extends AutomationJob {
    private SessionCleanupJobParameters parameters;
    private SessionCleanupJobData data;

    public SessionCleanupJob() {
        this.parameters = new SessionCleanupJobParameters();
        this.data = new SessionCleanupJobData(this, parameters);
    }

    @Override
    public void runJob(AutomationEnvironment env, AutomationProgress progress) {
        Model m = Model.getSingleton();
        Session s = m.getSession();
        if (this.getParameters().getClearAlerts()) {
            ExtensionAlert ea =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
            ea.deleteAllAlerts();
        }
        if (this.getParameters().getClearSites()) {
            SiteMap sm = s.getSiteTree();
            sm.getRoot().removeAllChildren();
            sm.reload();
        }
    }

    @Override
    public String getType() {
        return "session-cleanup";
    }

    @Override
    public Order getOrder() {
        return Order.RUN_FIRST;
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
    public SessionCleanupJobData getData() {
        return data;
    }

    @Override
    public SessionCleanupJobParameters getParameters() {
        return parameters;
    }

    @Override
    public void showDialog() {
        new SessionCleanupJobDialog(this).setVisible(true);
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

    @Override
    public void applyParameters(AutomationProgress progress) {
        // Doesnt need to do anything
    }

    @Override
    public String getTemplateDataMin() {
        return ExtensionAutomationEnhancements.getResourceAsString(
                "jobs/" + this.getType() + "-min.yaml");
    }

    @Override
    public String getTemplateDataMax() {
        return ExtensionAutomationEnhancements.getResourceAsString(
                "jobs/" + this.getType() + "-max.yaml");
    }
}
