/*
 * PlayStateSubscriptionParameterizedTest
 * Connect SDK
 *
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 08 Jul 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.connectsdk.service;


import com.amazon.whisperplay.fling.media.controller.RemoteMediaPlayer;
import com.amazon.whisperplay.fling.media.service.MediaPlayerStatus;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class PlayStateSubscriptionParameterizedTest {

    private final MediaControl.PlayStateStatus sdkMediaState;

    private MediaControl.PlayStateStatus anotherSdkMediaState;

    private final MediaPlayerStatus.MediaState fireTVMediaState;

    private final MediaPlayerStatus.MediaState anotherFireTVMediaState;

    private final RemoteMediaPlayer remoteMediaPlayer;

    private final FireTVService service;

    private final MediaControl.PlayStateListener listener;

    private final FireTVService.PlayStateSubscription subscription;

    static List<MediaPlayerStatus.MediaState> mediaStateValues =
            Arrays.asList(new MediaPlayerStatus.MediaState[] {
        MediaPlayerStatus.MediaState.NoSource,
        MediaPlayerStatus.MediaState.Error,
        MediaPlayerStatus.MediaState.Finished,
        MediaPlayerStatus.MediaState.Paused,
        MediaPlayerStatus.MediaState.Playing,
        MediaPlayerStatus.MediaState.PreparingMedia,
        MediaPlayerStatus.MediaState.ReadyToPlay,
        MediaPlayerStatus.MediaState.Seeking,
    });

    static List<MediaPlayerStatus.MediaState> anotherMediaStateValues =
            Arrays.asList(new MediaPlayerStatus.MediaState[] {
        MediaPlayerStatus.MediaState.Error,
        MediaPlayerStatus.MediaState.NoSource,
        MediaPlayerStatus.MediaState.NoSource,
        MediaPlayerStatus.MediaState.NoSource,
        MediaPlayerStatus.MediaState.NoSource,
        MediaPlayerStatus.MediaState.NoSource,
        MediaPlayerStatus.MediaState.NoSource,
        MediaPlayerStatus.MediaState.NoSource,
    });

    static List<MediaControl.PlayStateStatus> playStateValues =
            Arrays.asList(new MediaControl.PlayStateStatus[] {
        MediaControl.PlayStateStatus.Idle,
        MediaControl.PlayStateStatus.Unknown,
        MediaControl.PlayStateStatus.Finished,
        MediaControl.PlayStateStatus.Paused,
        MediaControl.PlayStateStatus.Playing,
        MediaControl.PlayStateStatus.Buffering,
        MediaControl.PlayStateStatus.Unknown,
        MediaControl.PlayStateStatus.Unknown,
    });

    static List<MediaControl.PlayStateStatus> anotherPlayStateValues =
            Arrays.asList(new MediaControl.PlayStateStatus[] {
        MediaControl.PlayStateStatus.Unknown,
        MediaControl.PlayStateStatus.Idle,
        MediaControl.PlayStateStatus.Idle,
        MediaControl.PlayStateStatus.Idle,
        MediaControl.PlayStateStatus.Idle,
        MediaControl.PlayStateStatus.Idle,
        MediaControl.PlayStateStatus.Idle,
        MediaControl.PlayStateStatus.Idle,
    });

    // we use indexes here instead of real argument because of ParameterizedRobolectricTestRunner
    // limitations for enum types
    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {0},{1},{2},{3},{4},{5},{6},{7},
        });
    }

    public PlayStateSubscriptionParameterizedTest(int index) {
        this.fireTVMediaState = mediaStateValues.get(index);
        this.anotherFireTVMediaState = anotherMediaStateValues.get(index);
        this.sdkMediaState = playStateValues.get(index);
        this.anotherSdkMediaState = anotherPlayStateValues.get(index);

        ServiceDescription serviceDescription = Mockito.mock(ServiceDescription.class);
        ServiceConfig serviceConfig = Mockito.mock(ServiceConfig.class);
        remoteMediaPlayer = Mockito.mock(RemoteMediaPlayer.class);
        Mockito.when(serviceDescription.getDevice()).thenReturn(remoteMediaPlayer);
        service = new FireTVService(serviceDescription, serviceConfig);
        listener = Mockito.mock(MediaControl.PlayStateListener.class);
        subscription = service.new PlayStateSubscription(listener);
    }

    @Test
    public void testStatusChange() {
        // given
        MediaControl.PlayStateListener secondListener =
                Mockito.mock(MediaControl.PlayStateListener.class);
        subscription.listeners.add(secondListener);

        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(fireTVMediaState);

        // when
        subscription.onStatusChange(status, 10);

        // then
        Mockito.verify(listener).onSuccess(sdkMediaState);
        Mockito.verify(secondListener).onSuccess(sdkMediaState);
    }

    @Test
    public void testStatusChangeRepeat() {
        // given
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(fireTVMediaState);

        // when
        subscription.onStatusChange(status, 10);
        subscription.onStatusChange(status, 20);

        // then
        Mockito.verify(listener, Mockito.times(1)).onSuccess(sdkMediaState);
    }

    @Test
    public void testStatusChangeRepeatThreeTimes() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(fireTVMediaState);
        MediaPlayerStatus anotherStatus = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(anotherStatus.getState()).thenReturn(anotherFireTVMediaState);


        subscription.onStatusChange(status, 10);
        subscription.onStatusChange(status, 20);
        Mockito.verify(listener, Mockito.times(1)).onSuccess(sdkMediaState);

        subscription.onStatusChange(anotherStatus, 20);
        Mockito.verify(listener, Mockito.times(1)).onSuccess(anotherSdkMediaState);
    }
}
