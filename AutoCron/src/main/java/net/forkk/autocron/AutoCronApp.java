/*
 * Copyright 2013 Andrew Okin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.forkk.autocron;

import android.app.Application;
import android.content.Intent;

import net.forkk.autocron.data.AutomationService;
import net.forkk.autocron.data.CustomRuleService;
import net.forkk.autocron.data.NfcService;
import net.forkk.autocron.data.action.ActionType;
import net.forkk.autocron.data.rule.RuleType;
import net.forkk.autocron.data.trigger.TriggerType;


/**
 * AutoCron's application context.
 */
public class AutoCronApp extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Load rule, action, and trigger types.
        RuleType.initialize(this);
        ActionType.initialize(this);
        TriggerType.initialize(this);

        // Start services.
        startService(new Intent(this, AutomationService.class));
        startService(new Intent(this, CustomRuleService.class));
        startService(new Intent(this, NfcService.class));
    }
}
