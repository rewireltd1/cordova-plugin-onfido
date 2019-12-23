package com.plugin.onfido;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;

import com.onfido.android.sdk.capture.DocumentType;
import com.onfido.android.sdk.capture.ExitCode;
import com.onfido.android.sdk.capture.Onfido;
import com.onfido.android.sdk.capture.OnfidoConfig;
import com.onfido.android.sdk.capture.OnfidoFactory;
import com.onfido.android.sdk.capture.errors.OnfidoException;
import com.onfido.android.sdk.capture.ui.camera.face.FaceCaptureVariant;
import com.onfido.android.sdk.capture.ui.camera.face.FaceCaptureStep;
import com.onfido.android.sdk.capture.ui.options.CaptureScreenStep;
import com.onfido.android.sdk.capture.ui.options.FlowStep;
import com.onfido.android.sdk.capture.upload.Captures;
import com.onfido.android.sdk.capture.upload.DocumentSide;
import com.onfido.android.sdk.capture.utils.CountryCode;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OnfidoActivity extends Activity {
    private Onfido client;
    private boolean firstTime = true;
    private static final String TAG = "OnFidoBridge";

    private Map<String, FlowStep> createMapStringToFlowStep() {
        HashMap flowStepMapping = new HashMap<String, FlowStep>();

        flowStepMapping.put("welcome", FlowStep.WELCOME);
        flowStepMapping.put("document", FlowStep.CAPTURE_DOCUMENT);

        for (CountryCode sCountryCode : CountryCode.values()) {
            flowStepMapping.put("license." + sCountryCode.getAlpha3().toLowerCase(),
                    new CaptureScreenStep(DocumentType.DRIVING_LICENCE, sCountryCode));
        }

        flowStepMapping.put("face", FlowStep.CAPTURE_FACE);
        flowStepMapping.put("face_video", new FaceCaptureStep(FaceCaptureVariant.VIDEO));
        flowStepMapping.put("final", FlowStep.FINAL);

        return flowStepMapping;
    }

    private FlowStep[] generateFlowStep(ArrayList<String> flowSteps) {
        Map<String, FlowStep> mapping = createMapStringToFlowStep();
        FlowStep[] steps = new FlowStep[flowSteps.size()];

        for (int i = 0; i < flowSteps.size(); i++) {
            steps[i] = mapping.get(flowSteps.get(i));
        }

        return steps;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Write your code inside this condition
        // Here should start the process that expects the onActivityResult
        if (firstTime == true) {
            client = OnfidoFactory.create(this).getClient();

            Bundle extras = getIntent().getExtras();
            String applicantId = "";
            String token = "";
            ArrayList<String> flowSteps = null;
            if (extras != null) {
                applicantId = extras.getString("applicant_id");
                token = extras.getString("token");
                flowSteps = extras.getStringArrayList("flow_steps");
            }

            FlowStep[] flow = generateFlowStep(flowSteps);

            final OnfidoConfig config = OnfidoConfig.builder(this).withToken(token).withApplicant(applicantId)
                    .withCustomFlow(flow).build();
            client.startActivityForResult(this, /* must be an activity */
                    1, /*
                        * this request code will be important for you on onActivityResult() to identity
                        * the onfido callback
                        */
                    config);
        }
    }

    protected JSONObject buildCaptureJsonObject(Captures captures) throws JSONException {
        JSONObject captureJson = new JSONObject();
        if (captures.getDocument() == null) {
            captureJson.put("document", null);
        }

        JSONObject docJson = new JSONObject();

        DocumentSide frontSide = captures.getDocument().getFront();
        if (frontSide != null) {
            JSONObject docSideJson = new JSONObject();
            docSideJson.put("id", frontSide.getId());
            docSideJson.put("side", frontSide.getSide());
            docSideJson.put("type", frontSide.getType());

            docJson.put("front", docSideJson);
        }

        DocumentSide backSide = captures.getDocument().getBack();
        if (backSide != null) {
            JSONObject docSideJson = new JSONObject();
            docSideJson.put("id", backSide.getId());
            docSideJson.put("side", backSide.getSide());
            docSideJson.put("type", backSide.getType());

            docJson.put("back", docSideJson);
        }

        captureJson.put("document", docJson);

        return captureJson;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        client.handleActivityResult(resultCode, data, new Onfido.OnfidoResultListener() {
            @Override
            public void userCompleted(Captures captures) {
                Intent intent = new Intent();
                JSONObject captureJson;
                try {
                    captureJson = buildCaptureJsonObject(captures);
                } catch (JSONException e) {
                    Log.d(TAG, "userCompleted: failed to build json result");
                    return;
                }

                Log.d(TAG, "userCompleted: successfully returned data to plugin");
                intent.putExtra("data", captureJson.toString());
                setResult(Activity.RESULT_OK, intent);
                finish();// Exit of this activity !

            }

            @Override
            public void userExited(ExitCode exitCode) {
                Intent intent = new Intent();
                Log.d(TAG, "userExited: YES");
                setResult(Activity.RESULT_CANCELED, intent);
                finish();// Exit of this activity !
            }

            @Override
            public void onError(OnfidoException e) {
                Intent intent = new Intent();
                Log.d(TAG, "onError: YES");
                e.printStackTrace();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();// Exit of this activity !
            }
        });
    }
}
