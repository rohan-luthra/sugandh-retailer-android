package com.sbw.auder.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import java.io.IOException;

@SuppressLint("AppCompatCustomView")
public class AudioStreaming extends TextView {
    private static final String NULL_PARAMETER_ERROR = "`stopText`, `playText` and `loadingText`" +
            " must have some value, if `useIcons` is set to false. Set `useIcons` to true, or add strings to stopText`, " +
            "`playText` and `loadingText` in the AudioPlayerView.xml";
    private Context context;
    public MediaPlayer mediaPlayer;
    private String playText;
    private String stopText;
    private String loadingText;
    private String url;
    public boolean useIcons;
    public boolean audioReady;
    public boolean usesCustomIcons;

    //Callbacks
    public interface OnAudioPlayerViewListener {
        void onAudioPreparing();

        void onAudioReady();

        void onAudioFinished();
    }

    private AudioStreaming.OnAudioPlayerViewListener listener;

    private void sendCallbackAudioFinished() {
        if (listener != null)
            listener.onAudioFinished();
    }

    private void sendCallbackAudioReady() {
        if (listener != null)
            listener.onAudioReady();
    }

    private void sendCallbackAudioPreparing() {
        if (listener != null)
            listener.onAudioPreparing();
    }

    //Constructors
    public AudioStreaming(Context context) {
        super(context);
        this.context = context;
    }

    public AudioStreaming(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getAttributes(attrs);
    }

    public AudioStreaming(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        getAttributes(attrs);
    }

    public void getAttributes(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, afriwan.ahda.R.styleable.AudioStreaming, 0, 0);

        try {
            stopText = a.getString(afriwan.ahda.R.styleable.AudioStreaming_stopText);
            playText = a.getString(afriwan.ahda.R.styleable.AudioStreaming_playText);
            loadingText = a.getString(afriwan.ahda.R.styleable.AudioStreaming_loadingText);
            useIcons = a.getBoolean(afriwan.ahda.R.styleable.AudioStreaming_useIcons, true);

            if ((stopText != null && playText != null && loadingText != null) && useIcons)
                usesCustomIcons = true;
            else if ((stopText == null || playText == null || loadingText == null) && !useIcons)
                throw new UnsupportedOperationException(NULL_PARAMETER_ERROR);

        } finally {
            a.recycle();
        }
    }

    //Implementation
    public void withUrl(String url) {
        this.url = url;
        setUpMediaPlayer();
    }

    private void setUpMediaPlayer() {
        if (useIcons) {
            setUpFont();
        }
        setText(playText);
        this.setOnClickListener(onViewClickListener);
    }

    private void setUpFont() {
        if (!usesCustomIcons) {
            Typeface iconFont = Typeface.createFromAsset(context.getAssets(), "audio-player-view-font.ttf");
            setTypeface(iconFont);
            playText = getResources().getString(afriwan.ahda.R.string.playIcon);
            stopText = getResources().getString(afriwan.ahda.R.string.stopIcon);
            loadingText = getResources().getString(afriwan.ahda.R.string.loadingIcon);
        }
    }

    private OnClickListener onViewClickListener = new OnClickListener() {

        public void onClick(View v) {
            try {
                toggleAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void toggleAudio() throws IOException {
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            pause();
        else
            play();
    }

    private void play() throws IOException {
        // Todo check what happens after second time loading
        if (!audioReady) {

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);

            prepareAsync();

            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);

        } else
            playAudio();
    }

    private void prepareAsync() {
        mediaPlayer.prepareAsync();
        setTextLoading();
        sendCallbackAudioPreparing();
    }

    private void playAudio() {
        mediaPlayer.start();
        setText(stopText);
    }


    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            playAudio();
            audioReady = true;
            clearAnimation();
            sendCallbackAudioReady();
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            setText(playText);
            sendCallbackAudioFinished();
        }
    };


    private void setTextLoading() {
        setText(loadingText);
        if (useIcons)
            startAnimation();
    }

    private void startAnimation() {
        final Animation rotation = AnimationUtils.loadAnimation(context, afriwan.ahda.R.anim.rotate_indefinitely);
        this.startAnimation(rotation);
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            setText(playText);
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            audioReady = false;
        }
    }

}
