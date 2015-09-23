/*
 * Data Compression Proxy bridge for Overchan
 * Copyright (C) 2014-2015  miku-nyan <https://github.com/miku-nyan>
 *     
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nya.miku.dcpbridge.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import nya.miku.dcpbridge.DCPHandler;
import nya.miku.dcpbridge.DCPServer;
import nya.miku.dcpbridge.DCPSocketFactory;
import nya.miku.dcpbridge.Logger;
import nya.miku.dcpbridge.android.R;

public class LauncherActivity extends Activity {
    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);
        ((WebView) findViewById(R.id.help_view)).loadData(getString(R.string.help_html), "text/html", "UTF-8");
        final EditText portField = (EditText) findViewById(R.id.port_field);
        final CheckBox httpsChkbox = (CheckBox) findViewById(R.id.https_chkbox);
        final CheckBox replaceAcceptChkbox = (CheckBox) findViewById(R.id.replace_chkbox);
        final Button btnOnOff = (Button) findViewById(R.id.btn_on_off);
        
        Preferences preferences = AndroidApplication.instance.getPreferencess();
        portField.setText(Integer.toString(preferences.port));
        httpsChkbox.setChecked(preferences.https);
        replaceAcceptChkbox.setChecked(preferences.replaceAccept);
        
        boolean started = AndroidApplication.instance.server != null;
        btnOnOff.setText(started ? R.string.button_off : R.string.button_on);
        portField.setEnabled(!started);
        httpsChkbox.setEnabled(!started);
        replaceAcceptChkbox.setEnabled(!started);
        
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean started = AndroidApplication.instance.server != null;
                if (started) {
                    try {
                        try {
                            AndroidApplication.instance.server.stopServer();
                        } catch (Exception e) {
                            Logger.log(e);
                            Toast.makeText(LauncherActivity.this, e.getMessage() != null ? e.getMessage() : e.toString(), Toast.LENGTH_LONG).show();
                        }
                        AndroidApplication.instance.server = null;
                        removeNotification();
                        
                        btnOnOff.setText(R.string.button_on);
                        portField.setEnabled(true);
                        httpsChkbox.setEnabled(true);
                        replaceAcceptChkbox.setEnabled(true);
                    } catch (Exception e) {
                        Logger.log(e);
                        Toast.makeText(LauncherActivity.this, e.getMessage() != null ? e.getMessage() : e.toString(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        int port = Integer.parseInt(portField.getText().toString());
                        boolean https = httpsChkbox.isChecked();
                        boolean replace = replaceAcceptChkbox.isChecked();
                        AndroidApplication.instance.savePreferences(new Preferences(port, https, replace));
                        AndroidApplication.instance.server = new DCPServer(port,
                                https ? DCPSocketFactory.getHttpsSocketFactory() : DCPSocketFactory.getHttpSocketFactory(),
                                replace ? DCPHandler.getReplacingAccept() : DCPHandler.getDefault());
                        AndroidApplication.instance.server.start();
                        createNotification();
                        
                        btnOnOff.setText(R.string.button_off);
                        portField.setEnabled(false);
                        httpsChkbox.setEnabled(false);
                        replaceAcceptChkbox.setEnabled(false);
                    } catch (Exception e) {
                        Logger.log(e);
                        Toast.makeText(LauncherActivity.this, e.getMessage() != null ? e.getMessage() : e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    
    private void createNotification() {
        startService(new Intent(this, NotifService.class));
    }
    
    private void removeNotification() {
        stopService(new Intent(this, NotifService.class));
    }
}
