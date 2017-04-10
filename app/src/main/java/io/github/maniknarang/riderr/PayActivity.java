package io.github.maniknarang.riderr;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PayActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_input);
        if(getActionBar()!=null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        final CardInputWidget mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);
        Button payButton = (Button) findViewById(R.id.pay_button);
        payButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Card cardToSave = mCardInputWidget.getCard();
                if (cardToSave == null)
                {
                }
                else
                {
                    Stripe stripe = null;
                    try
                    {
                        stripe = new Stripe(getApplicationContext(), "pk_live_cGagGttUkWDTnMCMgOYzfZbe");
                    }
                    catch (AuthenticationException e)
                    {
                    }
                    stripe.createToken(cardToSave, new TokenCallback()
                        {
                            public void onSuccess(Token token)
                            {
                                new NetworkTask().execute(token);
                            }
                            public void onError(Exception error)
                            {
                                error.printStackTrace();
                            }
                        }
                    );
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent))
                {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                }
                else
                {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class NetworkTask extends AsyncTask<Token,Void,Void>
    {
        @Override
        protected Void doInBackground(Token... tokens)
        {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder().add("stripeToken",tokens[0].getId()).build();
            Request request = new Request.Builder()
                    .url("http://prgzz.eastus.cloudapp.azure.com/info.php")
                    .post(body)
                    .build();
            Response response = null;
            try
            {
                response = client.newCall(request).execute();
                Log.v("Response:",response.toString());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }
}
