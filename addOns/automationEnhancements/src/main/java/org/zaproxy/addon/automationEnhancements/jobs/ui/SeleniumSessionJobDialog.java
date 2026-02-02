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
package org.zaproxy.addon.automationEnhancements.jobs.ui;

import org.parosproxy.paros.view.View;
import org.zaproxy.addon.automationEnhancements.jobs.SeleniumSessionJob;
import org.zaproxy.zap.extension.selenium.ProvidedBrowserUI;
import org.zaproxy.zap.extension.selenium.ProvidedBrowsersComboBoxModel;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.StandardFieldsDialog;

@SuppressWarnings("serial")
public class SeleniumSessionJobDialog extends StandardFieldsDialog {
    private static final long serialVersionUID = 1L;

    private static final String TITLE = "automationEnhancements.seleniumSessionJob.jobData.title";
    public static final String NAME_PARAM =
            "automationEnhancements.seleniumSessionJob.jobData.name";
    public static final String BROWSER_PARAM =
            "automationEnhancements.seleniumSessionJob.jobData.browser";
    public static final String START_URL_PARAM =
            "automationEnhancements.seleniumSessionJob.jobData.startUrl";
    public static final String PLAN_SCRIPTS_PARAM =
            "automationEnhancements.seleniumSessionJob.jobData.planScriptsOnly";

    private SeleniumSessionJob job;
    private ProvidedBrowsersComboBoxModel cbModel;

    public SeleniumSessionJobDialog(SeleniumSessionJob job) {
        super(View.getSingleton().getMainFrame(), TITLE, DisplayUtils.getScaledDimension(400, 375));

        this.job = job;

        cbModel =
                this.job
                        .getExtAutomationEnhancements()
                        .getExtensionSelenium()
                        .createProvidedBrowsersComboBoxModel();
        cbModel.setIncludeUnconfigured(false);
        String currentBrowserId = cbModel.getSelectedItem().getBrowser().getId();
        for (int i = 0; i < cbModel.getSize(); i++) {
            ProvidedBrowserUI pbUI = cbModel.getElementAt(i);
            if (pbUI.getBrowser()
                    .getName()
                    .equals(this.job.getData().getParameters().getBrowser())) {
                currentBrowserId = pbUI.getBrowser().getId();
            }
        }
        cbModel.setSelectedBrowser(currentBrowserId);

        this.addTextField(NAME_PARAM, this.job.getData().getName());
        this.addComboField(BROWSER_PARAM, cbModel, false);
        this.addTextField(START_URL_PARAM, this.job.getData().getParameters().getStartUrl());
        this.addCheckBoxField(
                PLAN_SCRIPTS_PARAM, this.job.getData().getParameters().getPlanScriptsOnly());

        this.addPadding();
    }

    @Override
    public void save() {
        this.job.getData().setName(this.getStringValue(NAME_PARAM));
        this.job.getData().getParameters().setBrowser(this.cbModel.getSelectedItem().getName());
        this.job.getData().getParameters().setStartUrl(this.getStringValue(START_URL_PARAM));
        this.job
                .getData()
                .getParameters()
                .setPlanScriptsOnly(this.getBoolValue(PLAN_SCRIPTS_PARAM));
        this.job.resetAndSetChanged();
    }

    @Override
    public String validateFields() {
        return null;
    }
}
