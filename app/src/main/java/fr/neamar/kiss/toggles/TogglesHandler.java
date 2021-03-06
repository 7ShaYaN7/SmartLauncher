package fr.neamar.kiss.toggles;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.neamar.kiss.pojo.TogglePojo;

public class TogglesHandler {
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;
    private final BluetoothAdapter bluetoothAdapter;
    private final AudioManager audioManager;

    /**
     * Initialize managers
     *
     * @param context android context
     */
    public TogglesHandler(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
    }

    /**
     * Return the state for the specified pojo
     *
     * @param pojo item to look for
     * @return item state
     */
    public Boolean getState(TogglePojo pojo) {
        try {
            switch (pojo.settingName) {
                case "wifi":
                    return getWifiState();
                case "data":
                    return getDataState();
                case "bluetooth":
                    return getBluetoothState();
                case "silent":
                    return getSilentState();
                default:
                    Log.e("wtf", "Unsupported toggle for reading: " + pojo.settingName);
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("log", "Unsupported toggle for device: " + pojo.settingName);
            return null;
        }
    }

    public void setState(TogglePojo pojo, Boolean state) {
        try {
            switch (pojo.settingName) {
                case "wifi":
                    setWifiState(state);
                    break;
                case "data":
                    setDataState(state);
                    break;
                case "bluetooth":
                    setBluetoothState(state);
                    break;
                case "silent":
                    setSilentState(state);
                    break;
                default:
                    Log.e("wtf", "Unsupported toggle for update: " + pojo.settingName);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("log", "Unsupported toggle for device: " + pojo.settingName);
        }
    }

    private Boolean getWifiState() {
        return wifiManager.isWifiEnabled();
    }

    private void setWifiState(Boolean state) {
        wifiManager.setWifiEnabled(state);
    }

    private Boolean getDataState() {
        Method dataMtd;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
            dataMtd.setAccessible(true);
            return (Boolean) dataMtd.invoke(connectivityManager);
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setDataState(Boolean state) {
        Method dataMtd;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled",
                    boolean.class);
            dataMtd.setAccessible(true);
            dataMtd.invoke(connectivityManager, state);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private Boolean getBluetoothState() {
        return bluetoothAdapter.isEnabled();
    }

    private void setBluetoothState(Boolean state) {
        if (state)
            bluetoothAdapter.enable();
        else
            bluetoothAdapter.disable();
    }

    private Boolean getSilentState() {
        int state = audioManager.getRingerMode();
        return state == AudioManager.RINGER_MODE_SILENT || state == AudioManager.RINGER_MODE_VIBRATE;
    }

    private void setSilentState(Boolean state) {
        if (!state) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_RING,
                    audioManager.getStreamVolume(AudioManager.STREAM_RING),
                    AudioManager.FLAG_PLAY_SOUND);
        } else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_VIBRATE);
        }
    }
}
