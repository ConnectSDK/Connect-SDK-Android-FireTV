/*
 * PlayStateSubscriptionTest
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
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;



@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class PlayStateSubscriptionTest {

    private RemoteMediaPlayer remoteMediaPlayer;

    private FireTVService service;

    private FireTVService.PlayStateSubscription subscription;

    private MediaControl.PlayStateListener listener;

    @Before
    public void setUp() {
        ServiceDescription serviceDescription = Mockito.mock(ServiceDescription.class);
        ServiceConfig serviceConfig = Mockito.mock(ServiceConfig.class);
        remoteMediaPlayer = Mockito.mock(RemoteMediaPlayer.class);
        Mockito.when(serviceDescription.getDevice()).thenReturn(remoteMediaPlayer);
        service = new FireTVService(serviceDescription, serviceConfig);
        listener = Mockito.mock(MediaControl.PlayStateListener.class);
        subscription = service.new PlayStateSubscription(listener);
    }

    @Test
    public void testInitialState() {
        Assert.assertEquals(1, subscription.listeners.size());
        Assert.assertTrue(subscription.listeners.contains(listener));
    }

    @Test
    public void testAddListener() {
        subscription.addListener(Mockito.mock(MediaControl.PlayStateListener.class));
        Assert.assertEquals(2, subscription.listeners.size());
    }

    @Test
    public void testRemoveListener() {
        subscription.removeListener(listener);
        Assert.assertTrue(subscription.listeners.isEmpty());
    }

    @Test
    public void testUnsubscribe() {
        subscription.unsubscribe();

        Mockito.verify(remoteMediaPlayer).removeStatusListener(subscription);
    }

}
