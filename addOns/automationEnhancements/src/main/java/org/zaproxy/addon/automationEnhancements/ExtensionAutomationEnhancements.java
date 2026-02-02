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
package org.zaproxy.addon.automationEnhancements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.addon.automation.ExtensionAutomation;
import org.zaproxy.addon.automationEnhancements.jobs.SeleniumSessionJob;
import org.zaproxy.addon.automationEnhancements.jobs.SessionCleanupJob;
import org.zaproxy.addon.network.ExtensionNetwork;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.extension.selenium.ExtensionSelenium;

/**
 * A set of extension utilities for the automation framework.
 *
 * <p>{@link ExtensionAutomationEnhancements} classes are the main entry point for adding/loading
 * functionalities provided by the add-ons.
 *
 * @see #hook(ExtensionHook)
 */
public class ExtensionAutomationEnhancements extends ExtensionAdaptor {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionAutomationEnhancements";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    public static final String PREFIX = "automationEnhancements";

    /**
     * Relative path (from add-on package) to load add-on resources.
     *
     * @see Class#getResource(String)
     */
    private static final String RESOURCES = "resources";

    private static final Logger LOGGER =
            LogManager.getLogger(ExtensionAutomationEnhancements.class);

    private static final String RESOURCES_DIR =
            "/org/zaproxy/addon/automationEnhancements/resources/";

    private static final List<Class<? extends Extension>> EXTENSION_DEPENDENCIES =
            List.of(
                    ExtensionAutomation.class,
                    ExtensionSelenium.class,
                    ExtensionScript.class,
                    ExtensionNetwork.class,
                    ExtensionAlert.class);

    public static final String SCRIPT_TYPE_AUTOMATION_JOB = "automationjob";

    private ExtensionAutomation extAuto;
    private ExtensionSelenium extSelenium;
    private ExtensionScript extScript;

    private SeleniumSessionJob ssJob;
    private SessionCleanupJob scJob;

    private ScriptType automationJobScriptType;

    public ExtensionAutomationEnhancements() {
        super(NAME);
        setI18nPrefix(PREFIX);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        extSelenium = getExtension(ExtensionSelenium.class);
        extScript = getExtension(ExtensionScript.class);

        ssJob = new SeleniumSessionJob();
        scJob = new SessionCleanupJob();

        extAuto = getExtension(ExtensionAutomation.class);
        extAuto.registerAutomationJob(ssJob);
        extAuto.registerAutomationJob(scJob);

        automationJobScriptType =
                new ScriptType(
                        SCRIPT_TYPE_AUTOMATION_JOB,
                        "automationEnhancements.scripts.type.automationJob",
                        createIcon("/resource/icon/16/script-standalone.png"),
                        false);
        extScript.registerScriptType(automationJobScriptType);
    }

    @Override
    public boolean canUnload() {
        // The extension can be dynamically unloaded, all resources used/added can be freed/removed
        // from core.
        return true;
    }

    @Override
    public void unload() {
        super.unload();

        if (ssJob != null) {
            extAuto.unregisterAutomationJob(ssJob);
        }
        if (scJob != null) {
            extAuto.unregisterAutomationJob(scJob);
        }
        if (automationJobScriptType != null) {
            extScript.removeScriptType(automationJobScriptType);
        }
    }

    @Override
    public String getDescription() {
        return "Enhanced Automation Tools";
    }

    @Override
    public List<Class<? extends Extension>> getDependencies() {
        return EXTENSION_DEPENDENCIES;
    }

    public ExtensionSelenium getExtensionSelenium() {
        return extSelenium;
    }

    public static String getResourceAsString(String name) {
        try (InputStream in =
                ExtensionAutomationEnhancements.class.getResourceAsStream(RESOURCES_DIR + name)) {
            return new BufferedReader(new InputStreamReader(in))
                            .lines()
                            .collect(Collectors.joining("\n"))
                    + "\n";
        } catch (Exception e) {
            CommandLine.error(
                    Constant.messages.getString(
                            "automationEnhancements.error.nofile", RESOURCES_DIR + name));
        }
        return "";
    }

    private static <T extends Extension> T getExtension(Class<T> clazz) {
        return Control.getSingleton().getExtensionLoader().getExtension(clazz);
    }

    private ImageIcon createIcon(String resourcePath) {
        if (getView() == null) {
            return null;
        }
        return new ImageIcon(ExtensionScript.class.getResource(resourcePath));
    }
}
