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
package org.zaproxy.addon.automationEnhancements.scripts;

import static org.zaproxy.addon.automationEnhancements.ExtensionAutomationEnhancements.SCRIPT_TYPE_AUTOMATION_JOB;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.addon.automationEnhancements.ExtensionAutomationEnhancements;
import org.zaproxy.zap.extension.graaljs.GraalJsEngineWrapper;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptEngineWrapper;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.testutils.TestUtils;
import org.zaproxy.zap.utils.I18N;

public class AutomationJobScriptTestUnitTest extends TestUtils {
    @Override
    protected void setUpMessages() {
        I18N i18n = new I18N(Locale.ENGLISH);
        try {
            PropertyResourceBundle prb =
                    new PropertyResourceBundle(
                            ExtensionAutomationEnhancements.class.getResourceAsStream(
                                    "/org/zaproxy/addon/automationEnhancements/resources/Messages.properties"));
            i18n.addMessageBundle(ExtensionAutomationEnhancements.PREFIX, prb);
        } catch (IOException ex) {
            Logger.getLogger(AutomationJobScriptTestUnitTest.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        i18n.addMessageBundle(DEFAULT_CONTENT_TYPE, extensionResourceBundle);
        Constant.messages = i18n;
    }

    @BeforeEach
    void beforeEach() throws Exception {
        setUpZap();
        // Model.getSingleton().getOptionsParam().load(new ZapXmlConfiguration());
    }

    @AfterEach
    void afterEach() throws Exception {
        Constant.messages = null;
    }

    @Test
    void willEnforceInterface() throws IOException, ScriptException {
        ExtensionScript extScript = initExtensionScript();
        ScriptEngineWrapper sew =
                extScript.getEngineWrapper(extScript.getEngineNameForExtension("js"));
        ScriptType st =
                new ScriptType(
                        SCRIPT_TYPE_AUTOMATION_JOB,
                        "automationEnhancements.scripts.type.automationJob",
                        null,
                        false);
        extScript.registerScriptType(st);
        File f =
                this.getResourceAsTempFile(
                        "/org/zaproxy/addon/automationEnhancements/scripts/empty.js");
        ScriptWrapper sw = new ScriptWrapper("empty.js", "An Empty File", sew, st, false, f);
        extScript.loadScript(sw);
        AutomationJobScript ajs =
                extScript.getInterfaceWithOutAddOnLoader(sw, AutomationJobScript.class);
        Assertions.assertNull(ajs);
        f.delete();
    }

    @Test
    void willLoadInterface() throws IOException, ScriptException, NoSuchMethodException, Exception {
        ExtensionScript extScript = initExtensionScript();
        ScriptEngineWrapper sew =
                extScript.getEngineWrapper(extScript.getEngineNameForExtension("js"));
        ScriptType st =
                new ScriptType(
                        SCRIPT_TYPE_AUTOMATION_JOB,
                        "automationEnhancements.scripts.type.automationJob",
                        null,
                        false);
        extScript.registerScriptType(st);
        File f =
                this.getResourceAsTempFile(
                        "/org/zaproxy/addon/automationEnhancements/scripts/minimal.js");
        ScriptWrapper sw = new ScriptWrapper("minimal.js", "A Minimal File", sew, st, false, f);
        extScript.loadScript(sw);
        AutomationJobScript ajs =
                extScript.getInterfaceWithOutAddOnLoader(sw, AutomationJobScript.class);
        Assertions.assertNotNull(ajs);
        f.delete();
    }

    private ExtensionScript initExtensionScript() {
        ExtensionScript extScript = new ExtensionScript();
        ScriptEngineWrapper sew =
                extScript.getEngineWrapper(extScript.getEngineNameForExtension("js"));
        extScript.removeScriptEngineWrapper(sew);
        GraalJsEngineWrapper gew =
                new GraalJsEngineWrapper(this.getClass().getClassLoader(), List.of(), null);
        extScript.registerScriptEngineWrapper(gew);
        return extScript;
    }

    private String getFileExtension(String resourcePath) {
        int lastDotIndex = resourcePath.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < resourcePath.length() - 1) {
            return "." + resourcePath.substring(lastDotIndex + 1);
        }
        return ".tmp"; // Default suffix
    }

    private File getResourceAsTempFile(String resourcePath) throws IOException {
        // Use the class loader to get the resource as an InputStream
        InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        // Create a new empty temporary file in the default temporary-file directory
        // The prefix must be at least three characters long.
        Path tempFile = Files.createTempFile("resource-", getFileExtension(resourcePath));

        // Copy the data from the resource input stream to the temporary file
        try (InputStream in = inputStream) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile.toFile();
    }
}
