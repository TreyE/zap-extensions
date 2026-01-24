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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.addon.automation.AutomationEnvironment;
import org.zaproxy.addon.automation.AutomationJob;
import org.zaproxy.addon.automation.AutomationProgress;
import org.zaproxy.addon.automation.jobs.JobUtils;
import org.zaproxy.addon.network.ExtensionNetwork;
import org.zaproxy.addon.network.server.ServerInfo;
import org.zaproxy.addon.seleniumAutomation.ExtensionSeleniumAutomation;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.extension.selenium.ExtensionSelenium;
import org.zaproxy.zap.extension.selenium.ProvidedBrowserUI;
import org.zaproxy.zap.extension.selenium.SeleniumScript;
import org.zaproxy.zap.extension.selenium.SeleniumScriptUtils;

public class SeleniumSessionJob extends AutomationJob {

    private ExtensionSelenium extSelenium;
    private ExtensionScript extScript;
    private WebDriver wd;

    private SeleniumSessionJobParameters parameters;
    private SeleniumSessionJobData data;

    public SeleniumSessionJob() {
        this.parameters = new SeleniumSessionJobParameters();
        this.data = new SeleniumSessionJobData(this, parameters);
    }
    
    @Override
    public void runJob(AutomationEnvironment env, AutomationProgress progress) {
        ProvidedBrowserUI pbUI = this.getProvidedBrowserUI(this.getParameters().getBrowser());
        
        List<ScriptWrapper> scripts = extScript.getScripts(ExtensionSelenium.SCRIPT_TYPE_SELENIUM);
        List<ScriptWrapper> enabledScripts = new ArrayList<>();
        for (ScriptWrapper script : scripts) {
            if (script.isEnabled()) {
                enabledScripts.add(script);
                script.setEnabled(false);
            }
        }
        wd = extSelenium.getProxiedBrowserByName(this.getParameters().getBrowser(), this.getParameters().getStartUrl(), false);
        ExtensionNetwork extNetwork = Control.getSingleton().getExtensionLoader().getExtension(ExtensionNetwork.class);
        ServerInfo si = extNetwork.getMainProxyServerInfo();
        
        SeleniumScriptUtils ssu =
                new SeleniumScriptUtils(wd, HttpSender.PROXY_INITIATOR, pbUI.getBrowser().getId(), si.getAddress(), si.getPort());
        
        for (ScriptWrapper script : enabledScripts) {
            try {
                SeleniumScript s = extScript.getInterface(script, SeleniumScript.class);

                if (s != null) {
                    Runnable runnable
                            = () -> {
                                try {
                                    s.browserLaunched(ssu);
                                } catch (Exception e) {
                                    extScript.handleScriptException(script, e);
                                }
                            };
                    runnable.run();
                } else {
                    extScript.handleFailedScriptInterface(
                            script,
                            Constant.messages.getString(
                                    "selenium.scripts.interface.error", script.getName()));
                }

            } catch (Exception e) {
                extScript.handleScriptException(script, e);
            }
        }
        for (ScriptWrapper escript : enabledScripts) {
            escript.setEnabled(true);
        }
        wd.quit();
    }

    @Override
    public String getType() {
        return "selenium-session";
    }

    @Override
    public Order getOrder() {
        return Order.EXPLORE;
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
    }

    @Override
    public void planStarted() {
        extSelenium = getExtSelenium();
        extScript = getExtScript();
    }

    @Override
    public String getTemplateDataMin() {
        return ExtensionSeleniumAutomation.getResourceAsString(this.getType() + "-min.yaml");
    }

    @Override
    public String getTemplateDataMax() {
        return ExtensionSeleniumAutomation.getResourceAsString(this.getType() + "-max.yaml");
    }

    public ExtensionSeleniumAutomation getExtSeleniumAutomation() {
        return Control.getSingleton().getExtensionLoader().getExtension(ExtensionSeleniumAutomation.class);
    }

    public ExtensionSelenium getExtSelenium() {
        return Control.getSingleton().getExtensionLoader().getExtension(ExtensionSelenium.class);
    }

    public ExtensionScript getExtScript() {
        return Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
    }

    @Override
    public SeleniumSessionJobData getData() {
        return data;
    }

    @Override
    public SeleniumSessionJobParameters getParameters() {
        return parameters;
    }

    @Override
    public void showDialog() {
        new SeleniumSessionJobDialog(this).setVisible(true);
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
    
    private ProvidedBrowserUI getProvidedBrowserUI(String browserName) {
        for (ProvidedBrowserUI provided : getExtSelenium().getProvidedBrowserUIList()) {
            if (provided.getName().equals(browserName)) {
                return provided;
            }
        }
        return null;
    }
}
