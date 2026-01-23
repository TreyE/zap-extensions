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
package org.zaproxy.addon.seleniumAutomation;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.addon.automation.ExtensionAutomation;
import org.zaproxy.addon.seleniumAutomation.jobs.SeleniumSessionJob;
import org.zaproxy.zap.extension.selenium.ExtensionSelenium;
import org.zaproxy.zap.extension.selenium.ProvidedBrowsersComboBoxModel;

/**
 * A set of extension utilities for automating Selenium.
 *
 * <p>{@link ExtensionSeleniumAutomation} classes are the main entry point for adding/loading
 * functionalities provided by the add-ons.
 *
 * @see #hook(ExtensionHook)
 */
public class ExtensionSeleniumAutomation extends ExtensionAdaptor {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionSeleniumAutomation";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    protected static final String PREFIX = "seleniumAutomation";

    /**
     * Relative path (from add-on package) to load add-on resources.
     *
     * @see Class#getResource(String)
     */
    private static final String RESOURCES = "resources";

    private static final Logger LOGGER = LogManager.getLogger(ExtensionSeleniumAutomation.class);

    private static final List<Class<? extends Extension>> EXTENSION_DEPENDENCIES =
            List.of(ExtensionAutomation.class, ExtensionSelenium.class);

    private ExtensionAutomation extAuto;
    private ExtensionSelenium extSelenium;

    private SeleniumSessionJob scJob;

    public ExtensionSeleniumAutomation() {
        super(NAME);
        setI18nPrefix(PREFIX);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);        

        extSelenium = getExtension(ExtensionSelenium.class);
        
        scJob = new SeleniumSessionJob();
        extAuto = getExtension(ExtensionAutomation.class);
        extAuto.registerAutomationJob(scJob);
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

        extAuto.unregisterAutomationJob(scJob);
    }

    @Override
    public String getDescription() {
        return "Enhanced Selenium Automation";
    }

    @Override
    public List<Class<? extends Extension>> getDependencies() {
        return EXTENSION_DEPENDENCIES;
    }
    
    public ExtensionSelenium getExtensionSelenium() {
        return extSelenium;
    }

    private static <T extends Extension> T getExtension(Class<T> clazz) {
        return Control.getSingleton().getExtensionLoader().getExtension(clazz);
    }
}
