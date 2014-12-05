/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.amms;
import javax.microedition.amms.Module;
import javax.microedition.media.Player;
import javax.microedition.media.MediaException;
import java.util.Hashtable;
import java.util.Enumeration;

abstract class BasicDirectModule extends AbstractDirectControllable 
        implements Module 
{
    private class PlayerConnectionInfo
    {
        private int _channels_mask;
        private boolean _added_wholly;
        
        PlayerConnectionInfo()
        {
            _added_wholly = true;
            _channels_mask = 0;
        }
        
        PlayerConnectionInfo( int channel )
        {
            _added_wholly = false;
            _channels_mask = ( 1 << channel );
        }
        
        boolean canAddMIDIChannel( int channel )
        {
            return ( ( _added_wholly == false ) && 
                    ( ( _channels_mask & ( 1 << channel ) ) == 0 ) );
        }
        
        boolean allChannelsRemoved()
        {
            return  ( _added_wholly == false && _channels_mask == 0 );
        }
        
        void addMIDIChannel( int channel )
        {
            _channels_mask |= ( 1 << channel );
        }

        boolean canRemoveMIDIChannel( int channel )
        {
            return ( _added_wholly == false && 
                    ( _channels_mask & ( 1 << channel ) ) != 0 );
        }
        
        boolean isAddedWholly()
        {
            return _added_wholly;
        }
        
        void removeMIDIChannel( int channel )
        {
            _channels_mask &= ~( 1 << channel );
        }
        
    }

    private Hashtable _players;
    
    BasicDirectModule()
    {
        _players = new Hashtable();
    }

    private static void checkChannelNumberRange( int channel_number )
    {
        if( channel_number < 0 || channel_number > 15 )
        {
            throw new IllegalArgumentException( 
                    "The MIDI channel number should be from 0 to 15" );
        }
    }
    
    private static boolean isPlayerMIDI( Player p )
    {
        return p.getContentType().toLowerCase().indexOf( "midi" ) != -1;
    }

    private static void checkIfPlayerIsMIDI( Player p )
    {
        if( !isPlayerMIDI( p ) )
        {
            throw new IllegalArgumentException( 
"JSR-234 Module: Cannot add/remove a MIDI channel of a " +
                    "Player that is not a MIDI Player" );
        }
        
    }
    
    private static void checkIfPlayerIsNull( Player player )
    {
        if( player == null )
        {
            throw new IllegalArgumentException( 
                    "JSR-234 Module: Cannot add/remove a null player" );
        }
    }

    private static boolean isPlayerStateAcceptable( Player p )
    {
        return ( p.getState() != Player.PREFETCHED &&
                 p.getState() != Player.STARTED );
    }
    
    private void checkPlayerStates( Player playerToAddOrRemove )
    {
        if( !isPlayerStateAcceptable( playerToAddOrRemove ) )
        {
            throw new IllegalStateException( 
"JSR-234 Module: Cannot add/remove a Player in PREFETCHED or STARTED state" );
        }
        
        Enumeration e = _players.keys();
        while( e.hasMoreElements() )
        {
            if( !isPlayerStateAcceptable( ( Player )e.nextElement() ) )
            {
                throw new IllegalStateException(
"JSR-234 Module: Cannot add/remove a Player when connected " +
                    "with any other Player in PREFETCHED or STARTED state" );
            }
        }
    }
    
    protected abstract void doAddMIDIChannel( Player player, int channel )
        throws MediaException;
            
    public void addMIDIChannel( Player player, int channel ) 
        throws MediaException
    {
        checkChannelNumberRange( channel );
        checkIfPlayerIsNull( player );
        checkIfPlayerIsMIDI( player );
        checkPlayerStates( player );
        
        PlayerConnectionInfo conn = 
                ( PlayerConnectionInfo )_players.get( player );
        if( conn != null )
        {
            if( !conn.canAddMIDIChannel( channel ) )
            {
                throw new IllegalArgumentException( 
"Cannot add a MIDI channel to a Module if either the channel or the whole " +
                        "Player is already part of the Module" );
            }
        }
        
        doAddMIDIChannel( player, channel );
        
        if( conn != null )
        {
            conn.addMIDIChannel( channel );
        }
        else
        {
            conn = new PlayerConnectionInfo( channel );
            _players.put( player, conn );
        }
        
    }

    protected abstract void doAddPlayer( Player player ) throws MediaException;
    public void addPlayer( Player player ) throws MediaException
    {
        checkIfPlayerIsNull( player );
        checkPlayerStates( player );
        
        if( _players.containsKey( player ) ) 
        {
            throw new IllegalArgumentException( 
"Cannot add a Player to a Module if either the Player or one " +
                    "of its MIDI channels is already part of the Module" );
        }
        
        doAddPlayer( player );
        
        _players.put( player, new PlayerConnectionInfo() );
    }

    protected abstract void doRemoveMIDIChannel( Player player, int channel );
    public void removeMIDIChannel( Player player, int channel )
    {
        checkChannelNumberRange( channel );
        checkIfPlayerIsNull( player );
        checkIfPlayerIsMIDI( player );
        checkPlayerStates( player );
        
        PlayerConnectionInfo conn = 
                ( PlayerConnectionInfo )_players.get( player );
        if( conn == null )
        {
            throw new IllegalArgumentException( 
"Cannot remove a MIDI channel that is not a part of the Module" );
        }
        if( !conn.canRemoveMIDIChannel( channel ) )
        {
            throw new IllegalArgumentException( 
"Cannot remove a MIDI channel that is not a part of the Module" );
        }
        
        doRemoveMIDIChannel( player, channel );
        
        conn.removeMIDIChannel( channel );
        if( conn.allChannelsRemoved() )
        {
            _players.remove( player );
        }
    }

    protected abstract void doRemovePlayer( Player player );
    public void removePlayer( Player player )
    {
        checkIfPlayerIsNull( player );
        checkPlayerStates( player );
        
        if( !isAddedWholly( player ) )
        {
            throw new IllegalArgumentException( 
"Cannot remove the Player because it is not a part of the Module" );
        }
            
        doRemovePlayer( player );
        
        _players.remove( player );
    }
    
    protected boolean isAddedWholly( Player p )
    {
        boolean added = false;
        PlayerConnectionInfo conn = 
                ( PlayerConnectionInfo )_players.get( p );
        if( conn != null  )
        {
            added = conn.isAddedWholly();
        }
        return added;
    }
}