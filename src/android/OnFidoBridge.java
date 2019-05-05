/**
 */
package com.plugin.onfido;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.rewire.app.R;

import java.util.ArrayList;

public class OnFidoBridge extends CordovaPlugin {
  private static final String TAG = "OnFidoBridge";
  private CallbackContext currentCallbackContext = null;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing OnFido");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("initOnfido")) {
      this.currentCallbackContext = callbackContext;
      final String token;
      final String applicantId;
      final ArrayList flowSteps = new ArrayList<String>();
      JSONArray flowStepsArray;

      try {
        JSONObject options = args.getJSONObject(0);
        token = options.getString("token");
        applicantId = options.getString("applicant_id");
        flowStepsArray = options.getJSONArray("flow_steps");
        for (int i = 0; i < flowStepsArray.length(); i++) {
          flowSteps.add(flowStepsArray.getString(i));
        }
      } catch (JSONException e) {
        callbackContext.error("Error encountered: " + e.getMessage());
        return false;
      }

      Intent intent = new Intent("com.plugin.onfido.OnfidoActivity");
      intent.putExtra("token", token);
      intent.putExtra("applicant_id", applicantId);
      intent.putExtra("flow_steps", flowSteps);
      cordova.startActivityForResult(this, intent, 1);
    }
    return true;
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if(resultCode == cordova.getActivity().RESULT_OK){
      Bundle extras = data.getExtras();// Get data sent by the Intent
      String information = extras.getString("data"); // data parameter will be send from the other activity.
      PluginResult resultado = new PluginResult(PluginResult.Status.OK, information);
      resultado.setKeepCallback(true);
      currentCallbackContext.sendPluginResult(resultado);
      return;
    }else if(resultCode == cordova.getActivity().RESULT_CANCELED){
      PluginResult resultado = new PluginResult(PluginResult.Status.OK, "canceled action, process this in javascript");
      resultado.setKeepCallback(true);
      currentCallbackContext.sendPluginResult(resultado);
      return;
    }
    // Handle other results if exists.
    super.onActivityResult(requestCode, resultCode, data);
  }
}