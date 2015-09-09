/*
 * FireTVServiceTest
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
import com.amazon.whisperplay.fling.media.service.CustomMediaPlayer;
import com.amazon.whisperplay.fling.media.service.MediaPlayerInfo;
import com.amazon.whisperplay.fling.media.service.MediaPlayerStatus;
import com.connectsdk.core.ImageInfo;
import com.connectsdk.core.MediaInfo;
import com.connectsdk.core.SubtitleInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.ExternalInputControl;
import com.connectsdk.service.capability.KeyControl;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.MouseControl;
import com.connectsdk.service.capability.PlaylistControl;
import com.connectsdk.service.capability.PowerControl;
import com.connectsdk.service.capability.TVControl;
import com.connectsdk.service.capability.TextInputControl;
import com.connectsdk.service.capability.ToastControl;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.FireTVServiceError;
import com.connectsdk.service.command.NotSupportedServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;
import com.connectsdk.service.sessions.LaunchSession;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class FireTVServiceTest {

    private RemoteMediaPlayer remoteMediaPlayer;

    private FireTVService service;

    @Before
    public void setUp() {
        ServiceDescription serviceDescription = Mockito.mock(ServiceDescription.class);
        ServiceConfig serviceConfig = Mockito.mock(ServiceConfig.class);
        remoteMediaPlayer = Mockito.mock(RemoteMediaPlayer.class);
        Mockito.when(serviceDescription.getDevice()).thenReturn(remoteMediaPlayer);
        service = new FireTVService(serviceDescription, serviceConfig);
    }

    @Test
    public void testInitialState() {
        Assert.assertNotNull(service);
    }

    @Test
    public void testGetCapabilities() {
        Set<String> requiredCapabilities = new HashSet<String>(Arrays.asList(new String[]{
                MediaPlayer.MediaInfo_Get,
                MediaPlayer.Display_Image,
                MediaPlayer.Play_Audio,
                MediaPlayer.Play_Video,
                MediaPlayer.Close,
                MediaPlayer.MetaData_MimeType,
                MediaPlayer.MetaData_Thumbnail,
                MediaPlayer.MetaData_Title,
                MediaPlayer.Subtitle_WebVTT,

                MediaControl.Play,
                MediaControl.Pause,
                MediaControl.Stop,
                MediaControl.Seek,
                MediaControl.Duration,
                MediaControl.Position,
                MediaControl.PlayState,
                MediaControl.PlayState_Subscribe,
        }));
        Set<String> capabilities = new HashSet<String>(service.getCapabilities());
        Assert.assertEquals(requiredCapabilities, capabilities);
    }

    @Test
    public void testGetFilter() {
        DiscoveryFilter filter = FireTVService.discoveryFilter();
        Assert.assertNotNull(filter);
        Assert.assertEquals(FireTVService.ID, filter.getServiceId());
        Assert.assertEquals(FireTVService.ID, filter.getServiceFilter());
    }

    @Test
    public void testGetPriorityLevel() {
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.HIGH,
                service.getPriorityLevel(MediaPlayer.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(ExternalInputControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(KeyControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(Launcher.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.HIGH,
                service.getPriorityLevel(MediaControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(MouseControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(PlaylistControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(PowerControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(TextInputControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(ToastControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(TVControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(VolumeControl.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(WebAppLauncher.class));
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.NOT_SUPPORTED,
                service.getPriorityLevel(null));
    }


    @Test
    public void testGetMediaPlayer() {
        Assert.assertSame(service, service.getMediaPlayer());
    }

    @Test
    public void testGetMediaPlayerCapabilityLevel() {
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.HIGH,
                service.getMediaPlayerCapabilityLevel());
    }

    @Test
    public void testGetMediaInfo() {
        MediaPlayer.MediaInfoListener listener = Mockito.mock(MediaPlayer.MediaInfoListener.class);
        MediaPlayerInfo info = Mockito.mock(MediaPlayerInfo.class);
        Mockito.when(info.getSource()).thenReturn("url");
        Mockito.when(info.getMetadata()).thenReturn(
                "{'title':'title','type':'video/mp4','description':'description'," +
                        "'poster':'poster','noreplay':true}");
        Mockito.when(remoteMediaPlayer.getMediaInfo())
                .thenReturn(new MockAsyncFuture<MediaPlayerInfo>(info));
        service.getMediaInfo(listener);
        Mockito.verify(remoteMediaPlayer).getMediaInfo();
        ArgumentCaptor<MediaInfo> argMediaInfo = ArgumentCaptor.forClass(MediaInfo.class);
        Mockito.verify(listener).onSuccess(argMediaInfo.capture());
        MediaInfo metadata = argMediaInfo.getValue();
        Assert.assertEquals("title", metadata.getTitle());
        Assert.assertEquals("description", metadata.getDescription());
        Assert.assertEquals("video/mp4", metadata.getMimeType());
        Assert.assertEquals("poster", metadata.getImages().get(0).getUrl());
    }

    @Test
    public void testGetMediaInfoWithException() {
        MediaPlayer.MediaInfoListener listener = Mockito.mock(MediaPlayer.MediaInfoListener.class);
        Mockito.when(remoteMediaPlayer.getMediaInfo())
                .thenReturn(new MockAsyncFutureFailure<MediaPlayerInfo>());
        service.getMediaInfo(listener);
        Mockito.verify(remoteMediaPlayer).getMediaInfo();
        verifyListenerError("Error getting media info", listener);
    }

    @Test
    public void testGetMediaInfoInWrongState() {
        MediaPlayer.MediaInfoListener listener = Mockito.mock(MediaPlayer.MediaInfoListener.class);
        Mockito.when(remoteMediaPlayer.getMediaInfo()).thenThrow(IOException.class);
        service.getMediaInfo(listener);
        Mockito.verify(remoteMediaPlayer).getMediaInfo();
        verifyListenerError("Error getting media info", listener);
    }

    @Test
    public void testSubscribeMediaInfo() {
        MediaPlayer.MediaInfoListener listener = Mockito.mock(MediaPlayer.MediaInfoListener.class);
        service.subscribeMediaInfo(listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }

    @Test
    public void testDisplayImage() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFuture<Void>(null));
        service.displayImage("url", "mime", "title", "description", "icon", launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'poster':'icon','noreplay':true}";
        verifySetMediaSource("url", metadata, true, false);
        verifyLauncherListener(launchListener);
    }

    @Test
    public void testDisplayImageWithNullMetadataFields() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFuture<Void>(null));
        service.displayImage("url", "mime", null, null, null, launchListener);
        String metadata = "{'type':'mime','noreplay':true}";
        verifySetMediaSource("url", metadata, true, false);
        verifyLauncherListener(launchListener);
    }

    @Test
    public void testDisplayImageWithEmptyMetadataFields() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFuture<Void>(null));
        service.displayImage("url", "mime", "", "", "", launchListener);
        String metadata = "{'type':'mime','noreplay':true}";
        verifySetMediaSource("url", metadata, true, false);
        verifyLauncherListener(launchListener);
    }

    @Test
    public void testDisplayImageWithNullFields() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFuture<Void>(null));
        service.displayImage(null, null, null, null, null, launchListener);
        String metadata = "{'noreplay':true}";
        verifySetMediaSource(null, metadata, true, false);
        verifyLauncherListener(launchListener);
    }

    @Test
    public void testDisplayImageWithMediaInfo() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFuture<Void>(null));
        MediaInfo mediaInfo = new MediaInfo("url", "mime", "title", "description");
        service.displayImage(mediaInfo, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
        verifyLauncherListener(launchListener);
    }

    @Test
    public void testGetMediaControl() {
        Assert.assertSame(service, service.getMediaControl());
    }

    @Test
    public void testGetMediaControlCapabilityLevel() {
        Assert.assertEquals(CapabilityMethods.CapabilityPriorityLevel.HIGH,
                service.getMediaControlCapabilityLevel());
    }

    @Test
    public void testPlayMedia() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFuture<Void>(null));
        service.playMedia("url", "mime", "title", "description", "icon", false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'poster':'icon','noreplay':true}";
        verifySetMediaSource("url", metadata, true, false);
        verifyLauncherListener(launchListener);
    }

    @Test
    public void testPlayMediaWithException() {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFutureFailure<Void>());
        service.playMedia("url", "mime", "title", "desc", "icon", false, launchListener);
        verifyListenerError("Error setting media source", launchListener);
    }

    @Test
    public void testPlayMediaInWrongState() {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenThrow(IllegalStateException.class);
        service.playMedia("url", "mime", "title", "desc", "icon", false, launchListener);
        verifyListenerError("Error setting media source", launchListener);
    }

    @Test
    public void testPlayMediaWithMediaInfo() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(remoteMediaPlayer.setMediaSource(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(new MockAsyncFuture<Void>(null));
        MediaInfo mediaInfo = new MediaInfo("url", "mime", "title", "description");
        service.playMedia(mediaInfo, false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
        verifyLauncherListener(launchListener);
    }

    @Test
    public void testPlayMediaWithMediaInfoWithEmptyImages() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        MediaInfo mediaInfo = new MediaInfo("url", "mime", "title", "description");
        mediaInfo.setImages(new ArrayList<ImageInfo>());
        service.playMedia(mediaInfo, false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
    }

    @Test
    public void testPlayMediaWithMediaInfoWithNullImages() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        MediaInfo mediaInfo = new MediaInfo("url", "mime", "title", "description");
        mediaInfo.setImages(null);
        service.playMedia(mediaInfo, false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
    }

    @Test
    public void testPlayMediaWithMediaInfoWithNullImage() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        MediaInfo mediaInfo = new MediaInfo("url", "mime", "title", "description");
        List<ImageInfo> images = new ArrayList<ImageInfo>(1);
        images.add(null);
        mediaInfo.setImages(images);
        service.playMedia(mediaInfo, false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
    }

    @Test
    public void testPlayMediaWithMediaInfoWithImage() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        MediaInfo mediaInfo = new MediaInfo("url", "mime", "title", "description");
        mediaInfo.addImages(new ImageInfo("imageUrl", ImageInfo.ImageType.Album_Art, 1, 1));
        service.playMedia(mediaInfo, false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'poster':'imageUrl','noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
    }

    @Test
    public void testPlayMediaWithSubtitles() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        MediaInfo mediaInfo = new MediaInfo.Builder("url", "mime")
                .setTitle("title")
                .setDescription("description")
                .setIcon("http://icon")
                .setSubtitleInfo(new SubtitleInfo.Builder("http://subtitleurl")
                        .setMimeType("subtitletype")
                        .setLabel("subtitlelabel")
                        .setLanguage("en")
                        .build())
                .build();

        service.playMedia(mediaInfo, false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'poster':'http://icon','tracks':[{'srclang':'en','label':'subtitlelabel'," +
                "'src':'http://subtitleurl','kind':'subtitles'}],'noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
    }

    @Test
    public void testPlayMediaWithOnlyRequiredFieldsInSubtitles() throws JSONException {
        MediaPlayer.LaunchListener launchListener = Mockito.mock(MediaPlayer.LaunchListener.class);
        MediaInfo mediaInfo = new MediaInfo.Builder("url", "mime")
                .setTitle("title")
                .setDescription("description")
                .setIcon("http://icon")
                .setSubtitleInfo(new SubtitleInfo.Builder("http://subtitleurl").build())
                .build();

        service.playMedia(mediaInfo, false, launchListener);
        String metadata = "{'title':'title','description':'description','type':'mime'," +
                "'poster':'http://icon','tracks':[{'srclang':'','label':'','src':'http://subtitleurl'," +
                "'kind':'subtitles'}],'noreplay':true}";
        verifySetMediaSource(mediaInfo.getUrl(), metadata, true, false);
    }

    @Test
    public void testClose() {
        LaunchSession launcherSession = Mockito.mock(LaunchSession.class);
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        service.closeMedia(launcherSession, listener);
        Mockito.verify(remoteMediaPlayer).stop();
    }

    @Test
    public void testPlay() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.play()).thenReturn(new MockAsyncFuture<Void>(null));
        service.play(listener);
        Mockito.verify(remoteMediaPlayer).play();
        Mockito.verify(listener).onSuccess(null);
    }

    @Test
    public void testPlayWithException() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.play()).thenReturn(new MockAsyncFutureFailure<Void>());
        service.play(listener);
        Mockito.verify(remoteMediaPlayer).play();
        verifyListenerError("Error playing", listener);
    }

    @Test
    public void testPlayInWrongState() {
        Mockito.when(remoteMediaPlayer.play()).thenThrow(IllegalStateException.class);
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        service.play(listener);
        Mockito.verify(remoteMediaPlayer).play();
        verifyListenerError("Error playing", listener);
    }

    @Test
    public void testPause() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.pause()).thenReturn(new MockAsyncFuture<Void>(null));
        service.pause(listener);
        Mockito.verify(remoteMediaPlayer).pause();
        Mockito.verify(listener).onSuccess(null);
    }

    @Test
    public void testPauseWithException() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.pause()).thenReturn(new MockAsyncFutureFailure<Void>());
        service.pause(listener);
        Mockito.verify(remoteMediaPlayer).pause();
        verifyListenerError("Error pausing", listener);
    }

    @Test
    public void testPauseInWrongState() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.pause()).thenThrow(IllegalStateException.class);
        service.pause(listener);
        Mockito.verify(remoteMediaPlayer).pause();
        verifyListenerError("Error pausing", listener);
    }

    @Test
    public void testStop() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.stop()).thenReturn(new MockAsyncFuture<Void>(null));
        service.stop(listener);
        Mockito.verify(remoteMediaPlayer).stop();
        Mockito.verify(listener).onSuccess(null);
    }

    @Test
    public void testStopWithException() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.stop()).thenReturn(new MockAsyncFutureFailure<Void>());
        service.stop(listener);
        Mockito.verify(remoteMediaPlayer).stop();
        verifyListenerError("Error stopping", listener);
    }

    @Test
    public void testStopInWrongState() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.stop()).thenThrow(IllegalStateException.class);
        service.stop(listener);
        Mockito.verify(remoteMediaPlayer).stop();
        verifyListenerError("Error stopping", listener);
    }

    @Test
    public void testRewind() {
        ResponseListener listener = Mockito.mock(ResponseListener.class);
        service.rewind(listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }

    @Test
    public void testFastForward() {
        ResponseListener listener = Mockito.mock(ResponseListener.class);
        service.fastForward(listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }

    @Test
    public void testPrevious() {
        ResponseListener listener = Mockito.mock(ResponseListener.class);
        service.previous(listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }

    @Test
    public void testNext() {
        ResponseListener listener = Mockito.mock(ResponseListener.class);
        service.next(listener);
        Mockito.verify(listener).onError(Mockito.isA(NotSupportedServiceCommandError.class));
    }

    @Test
    public void testSeek() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.seek(CustomMediaPlayer.PlayerSeekMode.Absolute, 777L))
                .thenReturn(new MockAsyncFuture<Void>(null));
        service.seek(777L, listener);
        Mockito.verify(remoteMediaPlayer).seek(
                Mockito.eq(CustomMediaPlayer.PlayerSeekMode.Absolute), Mockito.eq(777L));
        Mockito.verify(listener).onSuccess(null);
    }

    @Test
    public void testSeekWithException() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.seek(CustomMediaPlayer.PlayerSeekMode.Absolute, 777L))
                .thenReturn(new MockAsyncFutureFailure<Void>());
        service.seek(777L, listener);
        Mockito.verify(remoteMediaPlayer).seek(
                Mockito.eq(CustomMediaPlayer.PlayerSeekMode.Absolute), Mockito.eq(777L));
        verifyListenerError("Error seeking", listener);
    }

    @Test
    public void testSeekInWrongState() {
        ResponseListener<Object> listener = Mockito.mock(ResponseListener.class);
        Mockito.when(remoteMediaPlayer.seek(CustomMediaPlayer.PlayerSeekMode.Absolute, 777L))
                .thenThrow(IllegalStateException.class);
        service.seek(777L, listener);
        Mockito.verify(remoteMediaPlayer).seek(
                Mockito.eq(CustomMediaPlayer.PlayerSeekMode.Absolute), Mockito.eq(777L));
        verifyListenerError("Error seeking", listener);
    }

    @Test
    public void testGetDuration() {
        MediaControl.DurationListener listener = Mockito.mock(MediaControl.DurationListener.class);
        Mockito.when(remoteMediaPlayer.getDuration()).thenReturn(new MockAsyncFuture<Long>(123L));
        service.getDuration(listener);

        Mockito.verify(remoteMediaPlayer).getDuration();
        Mockito.verify(listener).onSuccess(Mockito.eq(123L));
    }

    @Test
    public void testGetDurationWithException() {
        MediaControl.DurationListener listener = Mockito.mock(MediaControl.DurationListener.class);
        Mockito.when(remoteMediaPlayer.getDuration())
                .thenReturn(new MockAsyncFutureFailure<Long>());
        service.getDuration(listener);
        Mockito.verify(remoteMediaPlayer).getDuration();
        verifyListenerError("Error getting duration", listener);
    }

    @Test
    public void testGetDurationWithInWrongState() {
        MediaControl.DurationListener listener = Mockito.mock(MediaControl.DurationListener.class);
        Mockito.when(remoteMediaPlayer.getDuration()).thenThrow(IllegalStateException.class);
        service.getDuration(listener);
        Mockito.verify(remoteMediaPlayer).getDuration();
        verifyListenerError("Error getting duration", listener);
    }

    @Test
    public void testGetPosition() {
        MediaControl.PositionListener listener = Mockito.mock(MediaControl.PositionListener.class);
        Mockito.when(remoteMediaPlayer.getPosition()).thenReturn(new MockAsyncFuture<Long>(123L));
        service.getPosition(listener);
        Mockito.verify(remoteMediaPlayer).getPosition();
        Mockito.verify(listener).onSuccess(Mockito.eq(123L));
    }

    @Test
    public void testGetPositionWithException() {
        MediaControl.PositionListener listener = Mockito.mock(MediaControl.PositionListener.class);
        Mockito.when(remoteMediaPlayer.getPosition())
                .thenReturn(new MockAsyncFutureFailure<Long>());
        service.getPosition(listener);
        Mockito.verify(remoteMediaPlayer).getPosition();
        verifyListenerError("Error getting position", listener);
    }

    @Test
    public void testGetPositionInWrongState() {
        MediaControl.PositionListener listener = Mockito.mock(MediaControl.PositionListener.class);
        Mockito.when(remoteMediaPlayer.getPosition()).thenThrow(IllegalStateException.class);
        service.getPosition(listener);
        Mockito.verify(remoteMediaPlayer).getPosition();
        verifyListenerError("Error getting position", listener);
    }

    @Test
    public void testGetPlayState() {
        MediaControl.PlayStateListener listener =
                Mockito.mock(MediaControl.PlayStateListener.class);
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Paused);
        Mockito.when(remoteMediaPlayer.getStatus())
                .thenReturn(new MockAsyncFuture<MediaPlayerStatus>(status));
        service.getPlayState(listener);
        Mockito.verify(remoteMediaPlayer).getStatus();
        Mockito.verify(listener).onSuccess(Mockito.eq(MediaControl.PlayStateStatus.Paused));
    }

    @Test
    public void testGetPlayStateWithException() {
        MediaControl.PlayStateListener listener = Mockito
                .mock(MediaControl.PlayStateListener.class);
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Paused);
        Mockito.when(remoteMediaPlayer.getStatus())
                .thenReturn(new MockAsyncFutureFailure<MediaPlayerStatus>());
        service.getPlayState(listener);
        Mockito.verify(remoteMediaPlayer).getStatus();
        verifyListenerError("Error getting play state", listener);
    }

    @Test
    public void testGetPlayStateInWrongState() {
        MediaControl.PlayStateListener listener = Mockito
                .mock(MediaControl.PlayStateListener.class);
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Paused);
        Mockito.when(remoteMediaPlayer.getStatus()).thenThrow(IllegalStateException.class);
        service.getPlayState(listener);
        Mockito.verify(remoteMediaPlayer).getStatus();
        verifyListenerError("Error getting play state", listener);
    }

    @Test
    public void testSubscribePlayState() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Paused);
        Mockito.when(remoteMediaPlayer.getStatus())
                .thenReturn(new MockAsyncFuture<MediaPlayerStatus>(status));

        MediaControl.PlayStateListener listener =
                Mockito.mock(MediaControl.PlayStateListener.class);
        service.subscribePlayState(listener);
        Mockito.verify(remoteMediaPlayer).addStatusListener(
                Mockito.any(CustomMediaPlayer.StatusListener.class));
        Mockito.verify(listener).onSuccess(Mockito.eq(MediaControl.PlayStateStatus.Paused));
    }

    @Test
    public void testSubscribePlayStateShouldBeSingle() {
        MediaControl.PlayStateListener listener =
                Mockito.mock(MediaControl.PlayStateListener.class);
        ServiceSubscription<MediaControl.PlayStateListener> subscription =
                service.subscribePlayState(listener);
        ServiceSubscription<MediaControl.PlayStateListener> subscription2 =
                service.subscribePlayState(listener);
        Assert.assertSame(subscription, subscription2);
    }

    @Test
    public void testSubscribePlayStateShouldAddNewListener() {
        MediaControl.PlayStateListener listenerFirst =
                Mockito.mock(MediaControl.PlayStateListener.class);
        MediaControl.PlayStateListener listenerSecond =
                Mockito.mock(MediaControl.PlayStateListener.class);

        ServiceSubscription<MediaControl.PlayStateListener> subscription =
                service.subscribePlayState(listenerFirst);
        Assert.assertEquals(1, subscription.getListeners().size());

        ServiceSubscription<MediaControl.PlayStateListener> subscription2 =
                service.subscribePlayState(listenerSecond);
        Assert.assertEquals(2, subscription2.getListeners().size());

        ServiceSubscription<MediaControl.PlayStateListener> subscription3 =
                service.subscribePlayState(listenerSecond);
        Assert.assertEquals(2, subscription2.getListeners().size());
    }

    @Test
    public void testDisconnectWithPlayStateSubscription() {
        MediaControl.PlayStateListener listener =
                Mockito.mock(MediaControl.PlayStateListener.class);
        CustomMediaPlayer.StatusListener subscription =
                (CustomMediaPlayer.StatusListener)service.subscribePlayState(listener);
        service.disconnect();
        Mockito.verify(remoteMediaPlayer).removeStatusListener(subscription);
    }

    @Test
    public void testDisconnectWithoutSubscription() {
        service.disconnect();
        Mockito.verify(remoteMediaPlayer, Mockito.times(0)).removeStatusListener(
                Mockito.any(CustomMediaPlayer.StatusListener.class));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusPaused() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Paused);
        Assert.assertEquals(MediaControl.PlayStateStatus.Paused,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusError() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Error);
        Assert.assertEquals(MediaControl.PlayStateStatus.Unknown,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusFinished() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Finished);
        Assert.assertEquals(MediaControl.PlayStateStatus.Finished,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusNoSource() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.NoSource);
        Assert.assertEquals(MediaControl.PlayStateStatus.Idle,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusPlaying() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Playing);
        Assert.assertEquals(MediaControl.PlayStateStatus.Playing,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusPreparing() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.PreparingMedia);
        Assert.assertEquals(MediaControl.PlayStateStatus.Buffering,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusReady() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.ReadyToPlay);
        Assert.assertEquals(MediaControl.PlayStateStatus.Unknown,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testCreatePlayStateStatusFromFireTVStatusSeeking() {
        MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
        Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Seeking);
        Assert.assertEquals(MediaControl.PlayStateStatus.Unknown,
                service.createPlayStateStatusFromFireTVStatus(status));
    }

    @Test
    public void testConnect() {
        ConnectableDevice listener = Mockito.mock(ConnectableDevice.class);
        service.setListener(listener);

        service.connect();

        Mockito.verify(listener).onConnectionSuccess(service);
        Assert.assertTrue(service.isConnected());
    }

    @Test
    public void testConnectWithNullRemoteMediaPlayer() {
        ServiceDescription serviceDescription = Mockito.mock(ServiceDescription.class);
        ServiceConfig serviceConfig = Mockito.mock(ServiceConfig.class);
        FireTVService service = new FireTVService(serviceDescription, serviceConfig);

        ConnectableDevice listener = Mockito.mock(ConnectableDevice.class);
        service.setListener(listener);

        service.connect();

        Mockito.verify(listener, Mockito.times(0)).onConnectionSuccess(service);
        Assert.assertFalse(service.isConnected());
    }

    @Test
    public void testIsConnectable() {
        Assert.assertTrue(service.isConnectable());
    }

    @Test
    public void testSubscribePlayStateWithNullListenerShouldNotCrash() {
        try {
            FireTVService.PlayStateSubscription subscription =
                    (FireTVService.PlayStateSubscription) service.subscribePlayState(null);
            MediaPlayerStatus status = Mockito.mock(MediaPlayerStatus.class);
            Mockito.when(status.getState()).thenReturn(MediaPlayerStatus.MediaState.Playing);
            subscription.onStatusChange(status, 0);
        } catch (RuntimeException e) {
            Assert.fail("subscribePlayState(null) should not throw a runtime exception");
        }
    }

    @Test
    public void testSubscribePlayStateWithNullStatusShouldReturnUnknownState() {
        MediaControl.PlayStateListener listener =
                Mockito.mock(MediaControl.PlayStateListener.class);
        FireTVService.PlayStateSubscription subscription =
                (FireTVService.PlayStateSubscription) service.subscribePlayState(listener);
        subscription.onStatusChange(null, 0);
        Mockito.verify(listener).onSuccess(MediaControl.PlayStateStatus.Unknown);
    }

    @Test
    public void testSubscribeMediaInfoShouldReturnNull() {
        Assert.assertNull(service.subscribeMediaInfo(null));
    }

    private void verifySetMediaSource(String source, String meta, boolean isAutoPlay,
                                      boolean isPlayInBg) throws JSONException {
        ArgumentCaptor<String> argSource = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argMeta = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> argAutoPlay = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Boolean> argPlayInBg = ArgumentCaptor.forClass(Boolean.class);
        Mockito.verify(remoteMediaPlayer).setMediaSource(argSource.capture(), argMeta.capture(),
                argAutoPlay.capture(), argPlayInBg.capture());

        Assert.assertEquals(source, argSource.getValue());
        Assert.assertEquals(new JSONObject(meta).toString(),
                new JSONObject(argMeta.getValue()).toString());
        Assert.assertEquals(isAutoPlay, argAutoPlay.getValue().booleanValue());
        Assert.assertEquals(isPlayInBg, argPlayInBg.getValue().booleanValue());
    }

    private void verifyLauncherListener(MediaPlayer.LaunchListener launchListener) {
        ArgumentCaptor<MediaPlayer.MediaLaunchObject> argMediaObject = ArgumentCaptor
                .forClass(MediaPlayer.MediaLaunchObject.class);
        Mockito.verify(launchListener).onSuccess(argMediaObject.capture());
        MediaPlayer.MediaLaunchObject mediaLaunchObject = argMediaObject.getValue();
        Assert.assertSame(service, mediaLaunchObject.mediaControl);
        Assert.assertEquals(null, mediaLaunchObject.playlistControl);
        Assert.assertEquals(LaunchSession.LaunchSessionType.Media,
                mediaLaunchObject.launchSession.getSessionType());
        Assert.assertSame(service, mediaLaunchObject.launchSession.getService());
    }

    private void verifyListenerError(String errorMessage, ResponseListener<?> listener) {
        ArgumentCaptor<FireTVServiceError> error = ArgumentCaptor
                .forClass(FireTVServiceError.class);
        Mockito.verify(listener).onError(error.capture());
        Assert.assertEquals(errorMessage, error.getValue().getMessage());
    }

    static class MockAsyncFuture<T> implements RemoteMediaPlayer.AsyncFuture<T> {

        private T value;

        public MockAsyncFuture(T value) {
            this.value = value;
        }

        @Override
        public void getAsync(RemoteMediaPlayer.FutureListener<T> futureListener) {
            futureListener.futureIsNow(this);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return value;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            return value;
        }
    }

    static class MockAsyncFutureFailure<T> implements RemoteMediaPlayer.AsyncFuture<T> {

        @Override
        public void getAsync(RemoteMediaPlayer.FutureListener<T> futureListener) {
            futureListener.futureIsNow(this);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            throw new ExecutionException("Operation error",
                    new Exception("MockAsyncFutureFailure"));
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            throw new ExecutionException("Operation error",
                    new Exception("MockAsyncFutureFailure"));
        }
    }

    static class DoubleMatcher extends ArgumentMatcher<Double> {

        private double expected;
        private double delta;

        public DoubleMatcher(double expected, double delta) {
            this.expected = expected;
            this.delta = delta;
        }

        @Override
        public boolean matches(final Object actual) {
            return Math.abs(expected - (Double) actual) <= delta;
        }
    }

    static class FloatMatcher extends ArgumentMatcher<Float> {

        private float expected;
        private float delta;

        public FloatMatcher(float expected, float delta) {
            this.expected = expected;
            this.delta = delta;
        }

        @Override
        public boolean matches(final Object actual) {
            return Math.abs(expected - (Float) actual) <= delta;
        }
    }

}
