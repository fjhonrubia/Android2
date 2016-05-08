package io.keepcoding.twlocator.dialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.keepcoding.twlocator.R;
import io.keepcoding.twlocator.model.Tweet;
import io.keepcoding.twlocator.model.TweetInfo;
import io.keepcoding.twlocator.model.TweetInfoSchema;
import io.keepcoding.twlocator.model.TweetSchema;

/**
 * Created by javi on 26/4/16.
 */
public class TweetDialog extends DialogFragment {

    @Bind(R.id.close_button)
    Button mButton;
    @Bind(R.id.user_name_text_view)
    TextView mTextView;
    @Bind(R.id.tweet_text_text_view) TextView mTxtTweetText;
    @Bind(R.id.tweet_image_view)
    ImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_tweet, container);

        ButterKnife.bind(this, view);

        String tweetId = "0";

        Bundle args = getArguments();
        if (args != null) {
            tweetId = args.getString("tweetId");
        }

        TweetSchema tweetSchema = new TweetSchema(getActivity());
        Tweet tweet = tweetSchema.query(Long.parseLong(tweetId));
        mTextView.setText(tweet.getUserName());
        mTxtTweetText.setText(tweet.getText());

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if(tweet.getURLUserPhotoProfile().length() != 0) {

            new DownloadImageTask((ImageView) view.findViewById(R.id.user_image_view)).execute(tweet.getURLUserPhotoProfile());

        }

        TweetInfoSchema tweetInfoSchema = new TweetInfoSchema(getActivity());
        ArrayList<TweetInfo> tweetInfoURLArrayList = tweetInfoSchema.query(tweet);

        if(tweetInfoURLArrayList.size() != 0) {

            new DownloadImageTask((ImageView) view.findViewById(R.id.tweet_image_view)).execute(tweetInfoURLArrayList.get(0).getText());

        }else{
            mImageView.setVisibility(View.GONE);
        }


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        int dialogWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
        int dialogHeight = LinearLayout.LayoutParams.WRAP_CONTENT;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

        Drawable d = new ColorDrawable(Color.BLACK);
        d.setAlpha(0);
        getDialog().getWindow().setBackgroundDrawable(d);

        WindowManager.LayoutParams wmlp = getDialog().getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER;
        getDialog().getWindow().setAttributes(wmlp);
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bmp = null;
            try {
                bmp = BitmapFactory.decodeStream(new URL(urlDisplay).openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
