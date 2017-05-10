package ai.api.sample;

import android.widget.TextView;

import com.google.gson.Gson;

import ai.api.android.GsonFactory;
import ai.api.model.AIResponse;

/**
 * Created by u1076070 on 5/10/2017.
 */

public class ParseResult {
    class response{

    }
    private Gson gson = GsonFactory.getGson();
    private final AIResponse response;
    private TextView resultTextView;
    ParseResult(AIResponse received_response){
        this.response = received_response;

    }
    public void print_response(){
        /* display cost of surgery */
        resultTextView.setText(gson.toJson(response));
    }
    private void parse_response(){
        /* get string item from response */

    }
}
