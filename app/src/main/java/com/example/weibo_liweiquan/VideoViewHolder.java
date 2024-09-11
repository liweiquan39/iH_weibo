package com.example.weibo_liweiquan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;



public class VideoViewHolder extends BaseViewHolder implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private Context context;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private String videoUrl;
    private Handler handler;
    private Runnable updateRunnable;
    private ImageView imageView, posterView, video_play, imageViewdetails1, imageViewdetails2;
    private SurfaceView surfaceView;
    private SeekBar seekBar;
    private TextView textViewname, textViewtitle, videoTime, like_text;
    private GridLayout gridLayout;
    private ConstraintLayout posterLayout;
    private ImageButton btn_like;


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public VideoViewHolder(View itemView, Context context, WeiboInfo weiboInfo) {
        super(itemView);
        this.context = context;

        imageView = itemView.findViewById(R.id.avatar);
        Glide.with(imageView)
                .load(weiboInfo.getAvatar())
                .transform(new FitCenter(), new RoundedCorners(40))
                .into(imageView);
        textViewname = itemView.findViewById(R.id.username);
        textViewtitle = itemView.findViewById(R.id.post_title);
        textViewname.setText(weiboInfo.getUsername());
        textViewtitle.setText(truncateString(weiboInfo.getTitle()));
        btn_like = itemView.findViewById(R.id.btn_like);
        like_text = itemView.findViewById(R.id.like_text);

        imageViewdetails1 = itemView.findViewById(R.id.single_image_1);
        imageViewdetails2 = itemView.findViewById(R.id.single_image_2);
        gridLayout = itemView.findViewById(R.id.image_grid);
        surfaceView = itemView.findViewById(R.id.surfaceView);
        videoTime = itemView.findViewById(R.id.tvDuration);
        seekBar = itemView.findViewById(R.id.seekBar);
        seekBar.setOnTouchListener((v, event) -> true);
        posterView = itemView.findViewById(R.id.videoPoster);
        video_play = itemView.findViewById(R.id.playimage);
        posterLayout = itemView.findViewById(R.id.poster_layout);

        if (weiboInfo.getLikeFlag()){
            btn_like.setImageResource(R.drawable.like_yes);
            int count = weiboInfo.getLikeCount();
            like_text.setText(Integer.toString(count));
            like_text.setTextColor(Color.parseColor("#EA512F"));
        }
        else{
            btn_like.setImageResource(R.drawable.ic_like);
            like_text.setText("点赞");
            like_text.setTextColor(Color.parseColor("#000000"));
        }

        btn_like = itemView.findViewById(R.id.btn_like);
        like_text = itemView.findViewById(R.id.like_text);

        View.OnClickListener likeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
                boolean isLoggedIn = preferences.getBoolean("success", false);
                if (isLoggedIn){
                    String token = preferences.getString("token", "");
                    boolean like = weiboInfo.getLikeFlag();
                    String url = like ? "https://hotfix-service-prod.g.mi.com/weibo/like/down" : "https://hotfix-service-prod.g.mi.com/weibo/like/up";
                    sendLikeRequest(url, token, weiboInfo.getId(), new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("ViewHolder", "Request Failed", e);
                            ((HomePageActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context,"当前无网络",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();
                                try {
                                    JSONObject jsonResponse = new JSONObject(responseBody);
                                    if (jsonResponse.getInt("code") == 200) {
                                        ((HomePageActivity) context).runOnUiThread(() -> {
                                            updateUI(weiboInfo);
                                            if (!like) {
                                                playLikeAnimation();
                                            } else {
                                                playUnlikeAnimation();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.i("okhttpmine", String.valueOf(response));
                        }
                    });
                } else {
                    Toast.makeText(context,"请先登录！",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                }
            }
        };
        like_text.setOnClickListener(likeClickListener);
        btn_like.setOnClickListener(likeClickListener);






        if (weiboInfo.getVideoUrl() != null){ //视频
            imageViewdetails1.setVisibility(View.GONE);
            imageViewdetails2.setVisibility(View.GONE);
            gridLayout.setVisibility(View.GONE);
            posterView.setVisibility(View.VISIBLE);
            video_play.setVisibility(View.VISIBLE);
            posterLayout.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);

            videoUrl = weiboInfo.getVideoUrl();

            Glide.with(posterView)
                    .load(weiboInfo.getPoster())
                    .transform(new FitCenter(), new RoundedCorners(40))
                    .into(posterView);



            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);

            handler = new Handler(Looper.getMainLooper());
            updateRunnable = new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        String currentDuration = formatTime(currentPosition);
                        String totalDuration = formatTime(mediaPlayer.getDuration());
                        videoTime.setText(currentDuration + "/" + totalDuration);
                        handler.postDelayed(this, 500);
                    }
                }
            };

            posterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    posterView.setVisibility(View.GONE);
                    video_play.setVisibility(View.GONE);
                    posterLayout.setVisibility(View.GONE);
                    surfaceView.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);
                    videoTime.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            togglePlayback();
                        }
                    }, 40);

                }
            });


            // 播放或暂停视频的点击事件
            surfaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePlayback();
                }
            });

        }
        else if(weiboInfo.getImages() != null){
            surfaceView.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
            posterView.setVisibility(View.GONE);
            video_play.setVisibility(View.GONE);
            posterLayout.setVisibility(View.GONE);
            videoTime.setVisibility(View.GONE);
            ArrayList<String> imageDetails = new ArrayList<>(weiboInfo.getImages());
            if (imageDetails.size()==1){
                gridLayout.setVisibility(View.GONE);

                imageViewdetails1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, FullScreenActivity.class);
                        intent.putStringArrayListExtra("imageUrls", imageDetails);
                        intent.putExtra("imageIndex", 0);
                        intent.putExtra("useravatar", weiboInfo.getAvatar());
                        intent.putExtra("username", weiboInfo.getUsername());
                        context.startActivity(intent);
                        ((HomePageActivity)context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });
                imageViewdetails2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, FullScreenActivity.class);
                        intent.putStringArrayListExtra("imageUrls", imageDetails);
                        intent.putExtra("imageIndex", 0);
                        intent.putExtra("useravatar", weiboInfo.getAvatar());
                        intent.putExtra("username", weiboInfo.getUsername());
                        context.startActivity(intent);
                        ((HomePageActivity)context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });


                Glide.with(context)
                        .asBitmap()
                        .load(imageDetails.get(0))
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                int width = resource.getWidth();
                                int height = resource.getHeight();

                                if (width > height) {
                                    // 宽大于高，显示横图
                                    imageViewdetails1.setVisibility(View.VISIBLE);
                                    imageViewdetails2.setVisibility(View.GONE);
                                    imageViewdetails1.setImageBitmap(resource);
                                } else {
                                    // 高大于宽，显示竖图
                                    imageViewdetails2.setVisibility(View.VISIBLE);
                                    imageViewdetails1.setVisibility(View.GONE);
                                    imageViewdetails2.setImageBitmap(resource);
                                }
                                return true;
                            }
                        })
                        .submit();

            }
            else{
                imageViewdetails1.setVisibility(View.GONE);
                imageViewdetails2.setVisibility(View.GONE);
                gridLayout.setVisibility(View.VISIBLE);
                gridLayout.removeAllViews();
                for(int i=0;i<imageDetails.size();i++){
                    ImageView imagesdetails = new ImageView(context);
                    imagesdetails.setVisibility(View.VISIBLE);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 300;
                    params.height = 300;
                    params.setMargins(2, 2, 2, 2);
                    imagesdetails.setLayoutParams(params);
                    imagesdetails.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    int finalI = i;
                    imagesdetails.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, FullScreenActivity.class);
                            intent.putStringArrayListExtra("imageUrls", imageDetails);
                            intent.putExtra("imageIndex", finalI);
                            intent.putExtra("useravatar", weiboInfo.getAvatar());
                            intent.putExtra("username", weiboInfo.getUsername());
                            context.startActivity(intent);
                            ((HomePageActivity)context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    });




                    gridLayout.addView(imagesdetails);

                    Glide.with(context)
                            .load(imageDetails.get(i))
                            .into(imagesdetails);

                }
            }
        }
        else{
            surfaceView.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
            posterView.setVisibility(View.GONE);
            videoTime.setVisibility(View.GONE);
            imageViewdetails1.setVisibility(View.GONE);
            imageViewdetails2.setVisibility(View.GONE);
            gridLayout.setVisibility(View.GONE);
            video_play.setVisibility(View.GONE);
            posterLayout.setVisibility(View.GONE);
        }
    }

    private void togglePlayback() {
        if (mediaPlayer == null) {
            startPlayback();
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                handler.removeCallbacks(updateRunnable);
            } else {
                mediaPlayer.start();
                handler.post(updateRunnable);
            }
        }
    }

    private void startPlayback() {
        releasePlayer();
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build());
        mediaPlayer.setOnCompletionListener(this);

        try {
            mediaPlayer.setDataSource(context, Uri.parse(videoUrl));
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int duration = mediaPlayer.getDuration();
                    seekBar.setMax(duration);
                    mediaPlayer.start();
                    handler.post(updateRunnable);
                }
            });
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            handler.removeCallbacks(updateRunnable);
            mediaPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {


    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        releasePlayer();
        surfaceView.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
        videoTime.setVisibility(View.GONE);
        posterView.setVisibility(View.VISIBLE);
        video_play.setVisibility(View.VISIBLE);
        posterLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.seekTo(0); // 循环播放
        mp.start();
    }
    public String truncateString(String input) {
        int maxLines = 6;
        int maxCharsPerLine = 22;

        String[] lines = input.split("\n");

        StringBuilder result = new StringBuilder();
        int linesCount = 0;
        for (String line : lines) {
            if (linesCount < maxLines) {
                if (line.length() <= maxCharsPerLine) {
                    result.append(line).append("\n");
                } else {
                    result.append(line.substring(0, maxCharsPerLine)).append("\n");
                }
                linesCount++;
            } else {
                break;
            }
        }
        String truncatedString = result.toString().trim();
        if (lines.length > maxLines || truncatedString.length() > maxLines * maxCharsPerLine) {
            truncatedString = truncatedString.substring(0, Math.min(truncatedString.length(), maxLines * maxCharsPerLine) - 3) + "..";
        }
        return truncatedString;
    }
    @SuppressLint("DefaultLocale")
    private String formatTime(int timeInMillis) {
        int totalSeconds = timeInMillis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    private void sendLikeRequest(String url, String token, Long id, Callback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();

        SharedPreferences sharedPreferences1 = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        TokenManager tokenManager = new TokenManager(sharedPreferences1, context);
        // 初始化OkHttpClient并添加拦截器，当接口请求报403时删除token
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
                .build();
        client.newCall(request).enqueue(callback);
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(WeiboInfo weiboInfo) {
        boolean like = weiboInfo.getLikeFlag();
        Log.i("like", String.valueOf(like));
        if (!like) { // 当前不喜欢点击变为喜欢
            btn_like.setImageResource(R.drawable.like_yes);
            int count = weiboInfo.getLikeCount();
            like_text.setText(Integer.toString(count));
            like_text.setTextColor(Color.parseColor("#EA512F"));
            weiboInfo.setLikeFlag(true);
        } else { // 当前喜欢点击变为不喜欢
            btn_like.setImageResource(R.drawable.ic_like);
            like_text.setText("点赞");
            like_text.setTextColor(Color.parseColor("#000000"));
            weiboInfo.setLikeFlag(false);
        }
    }
    private void playLikeAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn_like, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn_like, "scaleY", 1.0f, 1.2f, 1.0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(btn_like, "rotation", 0f, 360f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.setDuration(1000);
        animatorSet.start();
    }

    private void playUnlikeAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn_like, "scaleX", 1.0f, 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn_like, "scaleY", 1.0f, 0.8f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(1000);
        animatorSet.start();
    }
}
