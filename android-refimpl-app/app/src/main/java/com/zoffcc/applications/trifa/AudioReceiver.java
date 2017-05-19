package com.zoffcc.applications.trifa;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;

import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_play;
import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_play_length;
import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_read_write;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;

public class AudioReceiver extends Thread
{
    static final String TAG = "trifa.AudioReceiver";
    static boolean stopped = true;
    static boolean finished = true;

    // the audio recording options
    // static final int RECORDING_RATE = 48000; // 16000; // 44100;
    static final int CHANNEL_1 = AudioFormat.CHANNEL_OUT_MONO;
    static final int CHANNEL_2 = AudioFormat.CHANNEL_OUT_STEREO;
    static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // static final int CHANNELS_TOX = 1;
    static final long SMAPLINGRATE_TOX = 48000; // 16000;
    static long sampling_rate_ = SMAPLINGRATE_TOX;
    static int channels_ = 1;

    static int sleep_millis = 50; // TODO: hardcoded is bad!!!!
    static int buffer_size = 1920 * 5; // TODO: hardcoded is bad!!!!
    AudioTrack track = null;


    public AudioReceiver()
    {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        stopped = false;
        finished = false;
        start();
    }

    public AudioTrack findAudioTrack()
    {
        int CHANNEL;

        if (channels_ == 1)
        {
            CHANNEL = CHANNEL_1;
        }
        else
        {
            CHANNEL = CHANNEL_2;
        }

        int buffer_size22 = AudioTrack.getMinBufferSize((int) sampling_rate_, CHANNEL, FORMAT);
        Log.i(TAG, "audio_play:read:init min buffer size(x)=" + buffer_size);
        Log.i(TAG, "audio_play:read:init min buffer size(2)=" + buffer_size22);

        track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, (int) sampling_rate_, CHANNEL, FORMAT, buffer_size22 * 3, AudioTrack.MODE_STREAM);
        // track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, (int) sampling_rate_, CHANNEL, FORMAT, buffer_size * 3, AudioTrack.MODE_STREAM);

        try
        {
            MainActivity.AudioMode_old = audio_manager_s.getMode();
            MainActivity.RingerMode_old = audio_manager_s.getRingerMode();
            MainActivity.isSpeakerPhoneOn_old = audio_manager_s.isSpeakerphoneOn();

            if (Callstate.audio_speaker)
            {
                audio_manager_s.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audio_manager_s.setSpeakerphoneOn(true);
            }
            else
            {
                audio_manager_s.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audio_manager_s.setSpeakerphoneOn(false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return track;
    }

    @Override
    public void run()
    {
        Log.i(TAG, "Running Audio Thread");
        track = null;

        try
        {
            track = findAudioTrack();
            track.play();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        int res = 0;
        int read_bytes = 0;
        int played_bytes = 0;
        while (!stopped)
        {
            try
            {
                // puts data into "audio_buffer_play"
                // if ((audio_buffer_read_write(0, 0, 0, false)) && (audio_buffer_play_length > 0))
                if (audio_buffer_read_write(0, 0, 0, false))
                {
                    // Log.i(TAG, "audio_play:RecThread:1:len=" + audio_buffer_play_length);
                    played_bytes = track.write(audio_buffer_play.array(), 0, audio_buffer_play_length);
                    // Log.i(TAG, "audio_play:RecThread:2:len=" + audio_buffer_play_length);
                    // Log.i(TAG, "audio_play:recthr:play:bytes=" + played_bytes + " len=" + audio_buffer_play_length);
                }
                else
                {
                    try
                    {
                        // Log.i(TAG, "audio_play:recthr:no data");
                        Thread.sleep(sleep_millis);
                    }
                    catch (InterruptedException esleep)
                    {
                        // Log.i(TAG, "audio_play:recthr:wake up:" + esleep.getMessage());
                    }
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                Log.i(TAG, "audio_play:recthr:EE:" + e.getMessage());
            }
        }

        track.stop();
        track.release();

        try
        {
            audio_manager_s.setSpeakerphoneOn(MainActivity.isSpeakerPhoneOn_old);
            audio_manager_s.setMode(MainActivity.AudioMode_old);
            audio_manager_s.setRingerMode(MainActivity.RingerMode_old);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        finished = true;
    }

    public static void close()
    {
        stopped = true;
    }

}