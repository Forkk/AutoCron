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

package net.forkk.andcron.data.rule;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceFragment;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import net.forkk.andcron.R;
import net.forkk.andcron.data.Automation;
import net.forkk.andcron.data.AutomationService;
import net.forkk.andcron.data.GeofenceService;


/**
 * A location rule that uses geofences.
 */
public class LocationRule extends RuleBase
        implements GeofenceService.GeofenceClient, ServiceConnection
{
    public static final String LOGGER_TAG = AutomationService.LOGGER_TAG;

    private int mIntentListenerId;

    private PendingIntent mPendingIntent;

    private LocationClient mLocationClient;

    private GeofenceService.LocalBinder mBinder;

    private String mRequestId;

    public LocationRule(Automation parent, AutomationService service, int id)
    {
        super(parent, service, id);
    }

    /**
     * Called after the automation service finishes loading components. This should perform all
     * necessary initialization for this component.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    @Override
    public void onCreate(AutomationService service)
    {
        service.bindService(new Intent(service, GeofenceService.class), this,
                            Context.BIND_AUTO_CREATE);
    }

    /**
     * Called when the automation service is destroyed. This should perform all necessary cleanup.
     *
     * @param service
     *         The automation service that this component belongs to.
     */
    @Override
    public void onDestroy(AutomationService service)
    {
        mBinder.unregisterClient(mRequestId);

        service.unbindService(this);
    }

    private double getLatitude()
    {
        return getSharedPreferences().getFloat("latitude", 0);
    }

    private double getLongitude()
    {
        return getSharedPreferences().getFloat("longitude", 0);
    }

    private float getRadius()
    {
        return getSharedPreferences().getFloat("radius", 5);
    }

    @Override
    public void addPreferencesToFragment(PreferenceFragment fragment)
    {
        super.addPreferencesToFragment(fragment);
        fragment.addPreferencesFromResource(R.xml.prefs_location_rule);
    }

    /**
     * This should return the geofence that this client wants to register.
     *
     * @param requestId
     *         The request ID for the geofence. *If this request ID is not set, the geofence will
     *         not work!*
     *
     * @return This client's geofence.
     */
    @Override
    public Geofence getGeofence(String requestId)
    {
        mRequestId = requestId;

        Geofence.Builder builder = new Geofence.Builder();
        builder.setCircularRegion(getLatitude(), getLongitude(), getRadius());
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        builder.setRequestId(requestId);
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                   Geofence.GEOFENCE_TRANSITION_EXIT);
        return builder.build();
    }

    /**
     * Called when the device enters the geofence.
     */
    @Override
    public void onEnterGeofence()
    {
        setActive(true);
    }

    /**
     * Called when the device leaves the geofence.
     */
    @Override
    public void onLeaveGeofence()
    {
        setActive(false);
    }

    /**
     * Called when the service starts monitoring this client's geofence.
     */
    @Override
    public void onMonitoringStarted()
    {

    }

    /**
     * Called when the service stops monitoring this client's geofence.
     */
    @Override
    public void onMonitoringStopped()
    {
        setActive(false);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        mBinder = (GeofenceService.LocalBinder) iBinder;
        mBinder.registerClient(this, true);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {

    }

    @Override
    public String getTypeName()
    {
        return getResources().getString(R.string.location_rule_title);
    }
}