/*
 * FireTVDiscoveryProviderTest
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

package com.connectsdk.discovery.provider;

import com.amazon.whisperplay.fling.media.controller.DiscoveryController;
import com.amazon.whisperplay.fling.media.controller.RemoteMediaPlayer;
import com.connectsdk.discovery.DiscoveryProvider;
import com.connectsdk.discovery.DiscoveryProviderListener;
import com.connectsdk.service.FireTVService;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.ServiceDescription;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class FireTVDiscoveryProviderTest {

    private FireTVDiscoveryProvider provider;

    private DiscoveryController controller;

    @Before
    public void setUp() {
        controller = Mockito.mock(DiscoveryController.class);
        provider = new FireTVDiscoveryProvider(controller);
    }

    @Test
    public void testStartWhenNotRunning() {
        provider.start();
        Mockito.verify(controller, Mockito.times(1)).start(provider.fireTVListener);
    }

    @Test
    public void testStartWhenRunning() {
        provider.start();
        provider.start();
        Mockito.verify(controller, Mockito.times(1)).start(provider.fireTVListener);
    }

    @Test
    public void testStopWhenNotRunning() {
        provider.stop();
        Mockito.verify(controller, Mockito.times(0)).stop();
    }

    @Test
    public void testStopWhenRunning() {
        provider.start();
        provider.stop();
        Mockito.verify(controller, Mockito.times(1)).stop();
    }

    @Test
    public void testStopWithFoundServices() {
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);

        final ServiceDescription service1 = Mockito.mock(ServiceDescription.class);
        provider.foundServices.put("service1", service1);
        final ServiceDescription service2 = Mockito.mock(ServiceDescription.class);
        provider.foundServices.put("service2", service2);

        provider.stop();

        Mockito.verify(controller, Mockito.times(0)).stop();
        Mockito.verify(listener).onServiceRemoved(provider, service1);
        Mockito.verify(listener).onServiceRemoved(provider, service2);
        Assert.assertTrue(provider.foundServices.isEmpty());
    }

    @Test
    public void testAddListener() {
        // given
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        Assert.assertEquals(0, provider.serviceListeners.size());

        // when
        provider.addListener(listener);

        // then
        Assert.assertEquals(1, provider.serviceListeners.size());
    }

    @Test
    public void testRemoveListener() {
        // given
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);

        // when
        provider.removeListener(listener);

        // then
        Assert.assertEquals(0, provider.serviceListeners.size());
    }

    @Test
    public void testDiscoveryPlayerDiscovered() {
        // given
        RemoteMediaPlayer remoteMediaPlayer = mockRemoteMediaPlayer();
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);
        ArgumentCaptor<FireTVDiscoveryProvider> argDiscoveryProvider = ArgumentCaptor
                .forClass(FireTVDiscoveryProvider.class);
        ArgumentCaptor<ServiceDescription> argServiceDescription = ArgumentCaptor
                .forClass(ServiceDescription.class);

        // when
        provider.fireTVListener.playerDiscovered(remoteMediaPlayer);

        // then
        Mockito.verify(listener).onServiceAdded(argDiscoveryProvider.capture(),
                argServiceDescription.capture());

        // check all required fields in service description
        ServiceDescription serviceDescription = argServiceDescription.getValue();
        Assert.assertSame(provider, argDiscoveryProvider.getValue());
        Assert.assertSame(remoteMediaPlayer, serviceDescription.getDevice());
        Assert.assertFalse(provider.foundServices.isEmpty());
        Assert.assertEquals("FireTVDevice", serviceDescription.getFriendlyName());
        Assert.assertEquals("UID", serviceDescription.getIpAddress());
        Assert.assertEquals("UID", serviceDescription.getUUID());
        Assert.assertEquals(FireTVService.ID, serviceDescription.getServiceID());
        Assert.assertEquals(1, provider.foundServices.size());
    }

    @Test
    public void testDiscoveryPlayerDiscoveredTwice() {
        // given
        RemoteMediaPlayer remoteMediaPlayer = mockRemoteMediaPlayer();
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);
        ArgumentCaptor<FireTVDiscoveryProvider> argDiscoveryProvider = ArgumentCaptor
                .forClass(FireTVDiscoveryProvider.class);
        ArgumentCaptor<ServiceDescription> argServiceDescription = ArgumentCaptor
                .forClass(ServiceDescription.class);
        Assert.assertEquals(0, provider.foundServices.size());

        // when
        provider.fireTVListener.playerDiscovered(remoteMediaPlayer);
        Assert.assertEquals(1, provider.foundServices.size());

        Mockito.when(remoteMediaPlayer.getName()).thenReturn("UpdatedField");
        provider.fireTVListener.playerDiscovered(remoteMediaPlayer);

        // then
        Mockito.verify(listener).onServiceAdded(argDiscoveryProvider.capture(),
                argServiceDescription.capture());

        // check all required fields in service description
        ServiceDescription serviceDescription = argServiceDescription.getValue();
        Assert.assertSame(provider, argDiscoveryProvider.getValue());
        Assert.assertSame(remoteMediaPlayer, serviceDescription.getDevice());
        Assert.assertFalse(provider.foundServices.isEmpty());
        Assert.assertEquals("UpdatedField", serviceDescription.getFriendlyName());
        Assert.assertEquals("UID", serviceDescription.getIpAddress());
        Assert.assertEquals("UID", serviceDescription.getUUID());
        Assert.assertEquals(FireTVService.ID, serviceDescription.getServiceID());
        Assert.assertEquals(1, provider.foundServices.size());
    }

    @Test
    public void testDiscoveryNullPlayerDiscovered() {
        // given
        RemoteMediaPlayer remoteMediaPlayer = null;
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);

        try {
            provider.fireTVListener.playerDiscovered(remoteMediaPlayer);
        } catch (Exception e) {
            Assert.fail("playerLost should not throw exceptions");
        }

        // when
        Mockito.verify(listener, Mockito.times(0)).onServiceAdded(
                Mockito.any(DiscoveryProvider.class),
                Mockito.any(ServiceDescription.class));
        Assert.assertTrue(provider.foundServices.isEmpty());
    }

    @Test
    public void testDiscoveryPlayerLostWithEmptyProvider() {
        // given
        RemoteMediaPlayer remoteMediaPlayer = mockRemoteMediaPlayer();
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);

        // when
        provider.fireTVListener.playerLost(remoteMediaPlayer);

        // then
        Mockito.verify(listener, Mockito.times(0)).onServiceRemoved(Mockito.eq(provider),
                Mockito.any(ServiceDescription.class));
        Assert.assertTrue(provider.foundServices.isEmpty());
    }

    @Test
    public void testDiscoveryPlayerLost() {
        // given
        RemoteMediaPlayer remoteMediaPlayer = mockRemoteMediaPlayer();
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);
        provider.fireTVListener.playerDiscovered(remoteMediaPlayer);
        provider.fireTVListener.playerLost(remoteMediaPlayer);

        ArgumentCaptor<FireTVDiscoveryProvider> argDiscoveryProvider = ArgumentCaptor
                .forClass(FireTVDiscoveryProvider.class);
        ArgumentCaptor<ServiceDescription> argServiceDescription = ArgumentCaptor
                .forClass(ServiceDescription.class);

        // when
        Mockito.verify(listener).onServiceRemoved(argDiscoveryProvider.capture(),
                argServiceDescription.capture());

        // then
        ServiceDescription serviceDescription = argServiceDescription.getValue();
        Assert.assertSame(provider, argDiscoveryProvider.getValue());
        Assert.assertSame(remoteMediaPlayer, serviceDescription.getDevice());
        Assert.assertTrue(provider.foundServices.isEmpty());
    }

    @Test
    public void testDiscoveryNullPlayerLost() {
        // given
        RemoteMediaPlayer remoteMediaPlayer = null;
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);

        try {
            provider.fireTVListener.playerLost(remoteMediaPlayer);
        } catch (Exception e) {
            Assert.fail("playerLost should not throw exceptions");
        }

        // when
        Mockito.verify(listener, Mockito.times(0)).onServiceRemoved(
                Mockito.any(DiscoveryProvider.class),
                Mockito.any(ServiceDescription.class));
        Assert.assertTrue(provider.foundServices.isEmpty());
    }

    @Test
    public void testDiscoveryFailure() {
        // given
        DiscoveryProviderListener listener = Mockito.mock(DiscoveryProviderListener.class);
        provider.addListener(listener);

        // when
        provider.fireTVListener.discoveryFailure();

        // then
        Mockito.verify(listener).onServiceDiscoveryFailed(Mockito.eq(provider),
                Mockito.any(ServiceCommandError.class));
    }

    @Test
    public void testReset() {
        // given
        provider.fireTVListener.playerDiscovered(mockRemoteMediaPlayer());
        provider.start();

        // when
        provider.reset();

        // then
        Assert.assertTrue(provider.foundServices.isEmpty());
        Mockito.verify(controller).stop();
    }

    @Test
    public void testRestart() {
        // given
        provider.start();

        // when
        provider.restart();

        // then
        Mockito.verify(controller, Mockito.times(2)).start(
                Mockito.any(DiscoveryController.IDiscoveryListener.class));
        Mockito.verify(controller, Mockito.times(1)).stop();
    }

    @Test
    public void testRescan() {
        // given
        provider.start();

        // when
        provider.rescan();

        // then
        Mockito.verify(controller, Mockito.times(2)).start(
                Mockito.any(DiscoveryController.IDiscoveryListener.class));
        Mockito.verify(controller, Mockito.times(1)).stop();
    }

    @Test
    public void testInitialState() {
        FireTVDiscoveryProvider provider = new FireTVDiscoveryProvider(Robolectric.application);
        Assert.assertNotNull(provider.fireTVListener);
        Assert.assertNotNull(provider.foundServices);
        Assert.assertNotNull(provider.serviceListeners);
        Assert.assertTrue(provider.foundServices.isEmpty());
        Assert.assertTrue(provider.serviceListeners.isEmpty());
    }

    @Test
    public void testIsEmptyWithoutFoundServices() {
        Assert.assertTrue(provider.isEmpty());
    }

    @Test
    public void testIsEmptyWithFoundServices() {
        provider.fireTVListener.playerDiscovered(mockRemoteMediaPlayer());
        Assert.assertFalse(provider.isEmpty());
    }

    private RemoteMediaPlayer mockRemoteMediaPlayer() {
        RemoteMediaPlayer player = Mockito.mock(RemoteMediaPlayer.class);
        Mockito.when(player.getUniqueIdentifier()).thenReturn("UID");
        Mockito.when(player.getName()).thenReturn("FireTVDevice");
        return player;
    }

}
