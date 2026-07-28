package com.example.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return activeMediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = activeMediaSession?.player
        if (player != null && (!player.playWhenReady || player.mediaItemCount == 0)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        activeMediaSession = null
        super.onDestroy()
    }

    companion object {
        var activeMediaSession: MediaSession? = null
    }
}
