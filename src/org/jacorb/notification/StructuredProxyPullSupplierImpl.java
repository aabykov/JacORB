package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import java.util.List;

import org.jacorb.notification.interfaces.EventConsumer;
import org.jacorb.notification.interfaces.Message;
import org.jacorb.notification.queue.EventQueue;

import org.omg.CORBA.BooleanHolder;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.UnsupportedQoS;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierOperations;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPullSupplierPOATie;
import org.omg.CosNotifyComm.StructuredPullConsumer;
import org.omg.PortableServer.Servant;

/**
 * StructuredProxyPullSupplierImpl.java
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class StructuredProxyPullSupplierImpl
            extends AbstractProxy
            implements StructuredProxyPullSupplierOperations,
            EventConsumer
{

    StructuredPullConsumer structuredPullConsumer_;
    EventQueue pendingEvents_;

    static StructuredEvent undefinedStructuredEvent_;

    public StructuredProxyPullSupplierImpl( ConsumerAdminTieImpl myAdminServant,
                                            ApplicationContext appContext,
                                            ChannelContext channelContext,
                                            PropertyManager adminProperties,
                                            PropertyManager qosProperties,
                                            Integer key ) throws UnsupportedQoS
    {

        super( myAdminServant,
               appContext,
               channelContext,
               adminProperties,
               qosProperties,
               key );

        setProxyType( ProxyType.PULL_STRUCTURED );

        if ( undefinedStructuredEvent_ == null )
        {
            synchronized ( getClass() )
            {
                if ( undefinedStructuredEvent_ == null )
                {
                    undefinedStructuredEvent_ = new StructuredEvent();
                    EventType _type = new EventType();
                    FixedEventHeader _fixed = new FixedEventHeader( _type, "" );
                    Property[] _variable = new Property[ 0 ];
                    undefinedStructuredEvent_.header = new EventHeader( _fixed, _variable );
                    undefinedStructuredEvent_.filterable_data = new Property[ 0 ];
                    undefinedStructuredEvent_.remainder_of_body = appContext.getOrb().create_any();
                }
            }
        }

         pendingEvents_ = appContext.newEventQueue(qosProperties);
    }

    public void connect_structured_pull_consumer( StructuredPullConsumer consumer ) throws AlreadyConnected
    {
        if ( connected_ )
        {
            throw new AlreadyConnected();
        }

        connected_ = true;
        structuredPullConsumer_ = consumer;
    }


    public ConsumerAdmin MyAdmin()
    {
        return ( ConsumerAdmin ) myAdmin_.getThisRef();
    }

    public StructuredEvent pull_structured_event() throws Disconnected
    {
        StructuredEvent _event = null;
        BooleanHolder _hasEvent = new BooleanHolder();

        while ( true )
        {
            _event = try_pull_structured_event( _hasEvent );

            if ( _hasEvent.value )
            {
                return _event;
            }

            Thread.yield();
        }
    }

    public StructuredEvent try_pull_structured_event( BooleanHolder hasEvent ) throws Disconnected
    {
        if ( !connected_ )
            {
                throw new Disconnected();
            }

        try {
            Message _notificationEvent =
                pendingEvents_.getEvent(false);

                if (_notificationEvent != null) {

                    try {
                        hasEvent.value = true;
                        return _notificationEvent.toStructuredEvent();
                    } finally {
                        _notificationEvent.dispose();
                    }
                }
        } catch (InterruptedException e) {}

        hasEvent.value = false;
        return undefinedStructuredEvent_;

    }


    public void disconnect_structured_pull_supplier()
    {
        dispose();
    }

    public void disconnect_sequence_pull_supplier()
    {
        dispose();
    }

    private void disconnectClient()
    {
        if ( connected_ )
        {
            if ( structuredPullConsumer_ != null )
            {
                structuredPullConsumer_.disconnect_structured_pull_consumer();
                connected_ = false;
                structuredPullConsumer_ = null;
            }
        }
    }

    public void deliverEvent( Message event )
    {
        logger_.debug( "dispatchEvent" );

        pendingEvents_.put( event );

    }

    public List getSubsequentFilterStages()
    {
        return CollectionsWrapper.singletonList( this );
    }

    public EventConsumer getEventConsumer()
    {
        return this;
    }

    public boolean hasEventConsumer()
    {
        return true;
    }

    public void dispose()
    {
        super.dispose();
        disconnectClient();
    }

    public void disableDelivery()
    {
        // ignore
    }

    public void enableDelivery()
    {
        // ignore
    }

    public void deliverPendingEvents()
    {
        // can't do it
    }

    public Servant getServant()
    {
        if ( thisServant_ == null )
        {
            synchronized ( this )
            {
                if ( thisServant_ == null )
                {
                    thisServant_ = new StructuredProxyPullSupplierPOATie( this );
                }
            }
        }

        return thisServant_;
    }

    public boolean hasPendingEvents()
    {
        return !pendingEvents_.isEmpty();
    }
}
