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

package net.forkk.andcron.data;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A service that manages geofences.
 */
public class GeofenceService extends Service
        implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
                           GooglePlayServicesClient.OnConnectionFailedListener,
                           LocationClient.OnAddGeofencesResultListener,
                           LocationClient.OnRemoveGeofencesResultListener
{
    private static final String LOGGER_TAG = "net.forkk.andcron.GeofenceService";

    public static final String REQ_ID_PREFIX = "net.forkk.andcron.GeofenceService.";

    private HashMap<String, GeofenceClient> mGeofenceClients;

    private PendingIntent mGeofenceIntent;

    private LocationClient mLocationClient;

    /**
     * A list containing the IDs of the clients to use in the next add or remove operation.
     */
    private List<String> mNextOperationClients;

    /**
     * The operation to be performed next time the geofence service is connected.
     */
    private GeofenceOperationType mNextOperation;

    private enum GeofenceOperationType
    {
        NONE,
        ADD,
        REMOVE
    }

    private int mNextClientId;

    public GeofenceService()
    {
        mNextOperation = GeofenceOperationType.NONE;
        mGeofenceClients = new HashMap<String, GeofenceClient>();
        mNextOperationClients = new ArrayList<String>();
    }

    @Override
    public void onCreate()
    {
        Log.d(LOGGER_TAG, "Initializing geofence service.");

        super.onCreate();

        // Initialize and connect the location client.
        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    public void onDestroy()
    {
        Log.d(LOGGER_TAG, "Stopping geofence service.");

        super.onDestroy();

        // Disconnect the location client.
        mLocationClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        List<Geofence> triggers = LocationClient.getTriggeringGeofences(intent);

        if (triggers != null)
        {
            if (LocationClient.getGeofenceTransition(intent) == Geofence.GEOFENCE_TRANSITION_ENTER)
            {
                Log.d(LOGGER_TAG, "Entered geofence.");
                for (Geofence geofence : triggers)
                    mGeofenceClients.get(geofence.getRequestId()).onEnterGeofence();
            }
            else
            {
                Log.d(LOGGER_TAG, "Left geofence.");
                for (Geofence geofence : triggers)
                    mGeofenceClients.get(geofence.getRequestId()).onLeaveGeofence();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public static String idStringForInt(int id)
    {
        return REQ_ID_PREFIX + id;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.i(LOGGER_TAG, location.toString());
    }

    public class LocalBinder extends Binder
    {
        /**
         * Registers the given client with the geofence servie and returns its ID.
         *
         * @param client
         *         The client to register.
         * @param registerGeofences
         *         If true, immediately register this client's geofences. This causes a bit of
         *         overhead, so it's usually best to wait until all clients are registered when
         *         registering multiple clients.
         *
         * @return The client's request ID string.
         */
        public String registerClient(GeofenceClient client, boolean registerGeofences)
        {
            String idStr = idStringForInt(mNextClientId++);
            mGeofenceClients.put(idStr, client);
            registerGeofence(idStr);
            return idStr;
        }

        /**
         * Un-registers the geofence client with the given ID.
         *
         * @param id
         *         Request ID of the client to unregister.
         */
        public void unregisterClient(String id)
        {
            if (mGeofenceClients.containsKey(id))
            {
                mGeofenceClients.remove(id);
                unregisterGeofence(id);
            }
        }
    }

    /**
     * Registers the geofence for the client with the given ID.
     *
     * @param id
     *         The ID of the client whose geofence should be registered.
     */
    public void registerGeofence(String id)
    {
        mNextOperation = GeofenceOperationType.ADD;
        mNextOperationClients.add(id);
        if (mLocationClient.isConnected()) addGeofences();
        else mLocationClient.connect();
    }

    /**
     * Un-registers the geofence with the given ID.
     *
     * @param id
     *         The ID of the geofence to unregister.
     */
    public void unregisterGeofence(String id)
    {
        mNextOperation = GeofenceOperationType.REMOVE;
        mNextOperationClients.add(id);
        if (mLocationClient.isConnected()) removeGeofences();
        else mLocationClient.connect();
    }

    private void addGeofences()
    {
        Log.d(LOGGER_TAG, "Adding geofences.");
        List<Geofence> geofences = new ArrayList<Geofence>();
        for (String idStr : mNextOperationClients)
        {
            Geofence geofence = mGeofenceClients.get(idStr).getGeofence(idStr);
            assert geofence.getRequestId().equals(idStr);
            geofences.add(geofence);
        }

        Intent intent = new Intent(this, GeofenceService.class);
        mGeofenceIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mLocationClient.addGeofences(geofences, mGeofenceIntent, this);
    }

    private void removeGeofences()
    {
        Log.d(LOGGER_TAG, "Removing geofences.");
        mLocationClient.removeGeofences(mNextOperationClients, this);
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d(LOGGER_TAG, "Google Play location services connected.");

        switch (mNextOperation)
        {
        case ADD:
            addGeofences();
            break;

        case REMOVE:
            removeGeofences();
            break;

        case NONE:
            Log.w(LOGGER_TAG, "Location client connected with nothing to do.");
            break;
        }
    }

    @Override
    public void onDisconnected()
    {
        Log.e(LOGGER_TAG, "Disconnected from Google Play location services.");
        for (Map.Entry<String, GeofenceClient> entry : mGeofenceClients.entrySet())
            entry.getValue().onMonitoringStopped();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // TODO: Resolve failure if possible.
        Log.e(LOGGER_TAG, "Failed to connect to Google Play location service. Error: " +
                          connectionResult.getErrorCode());
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] strings)
    {
        if (statusCode == LocationStatusCodes.SUCCESS)
        {
            Log.i(LOGGER_TAG, strings.length + " geofences registered successfully.");
            for (String reqId : strings)
            {
                GeofenceClient client = mGeofenceClients.get(reqId);
                if (client == null) Log.w(LOGGER_TAG, "Null client.");
                else client.onMonitoringStarted();
            }
        }
        else
        {
            // TODO: Report error to user.
        }

        mNextOperation = GeofenceOperationType.NONE;
        mNextOperationClients.clear();
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] requestIDs)
    {
        for (String reqId : requestIDs)
        {
            GeofenceClient client = mGeofenceClients.get(reqId);
            if (client != null) client.onMonitoringStopped();
        }

        mNextOperation = GeofenceOperationType.NONE;
        mNextOperationClients.clear();
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent)
    {
        // TODO: Implement
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new LocalBinder();
    }

    /**
     * An interface for clients that need to use geofences.
     */
    public static interface GeofenceClient
    {
        /**
         * This should return the geofence that this client wants to register.
         *
         * @param requestId
         *         The request ID for the geofence. *If this request ID is not set, the geofence
         *         will not work!*
         *
         * @return This client's geofence.
         */
        public abstract Geofence getGeofence(String requestId);

        /**
         * Called when the device enters the geofence.
         */
        public abstract void onEnterGeofence();

        /**
         * Called when the device leaves the geofence.
         */
        public abstract void onLeaveGeofence();

        /**
         * Called when the service starts monitoring this client's geofence.
         */
        public abstract void onMonitoringStarted();

        /**
         * Called when the service stops monitoring this client's geofence.
         */
        public abstract void onMonitoringStopped();
    }
}
