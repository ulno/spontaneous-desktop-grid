/***************************************************************************
 *   Filename: f2fmain.h
 *   Author: ulno
 ***************************************************************************
 *   Copyright (C) 2008 by Ulrich Norbisrath
 *   devel@mail.ulno.net
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as
 *   published by the Free Software Foundation; either version 2 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the
 *   Free Software Foundation, Inc.,
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ***************************************************************************
 *   Description:
 *   This is the main part the F2FCore implementation
 ***************************************************************************/

#ifndef F2FMAIN_H_
#define F2FMAIN_H_

#include "f2fconfig.h"
#include "f2ftypes.h"
#include "f2fpeer.h"
#include "f2fgroup.h"
#include "f2fmessagetypes.h"


#ifdef __cplusplus
extern "C"
    {
#endif

    
/** return the Error code of the last problematic operation */
F2FError f2fGetErrorCode( );

/** Do the initialization - especially create a random seed and get your own PeerID
 * Must be called first.
 * Sets the name of this peer (for example "Ulrich Norbisrath's peer") and the public key.
 * In case of failure this function will return null and set the error code */
F2FPeer * f2fInit( const F2FString myName, const F2FString myPublicKey );

/** As a next step, the user has to create a new F2FGroup, in which his/her Job can be
 * computeted.
 * This group gets a name, which should be displayed in the invitation of clients (other peers).
 * The created group is returned. If an error occured null is returned and the errorcode is set */
F2FGroup * f2fCreateGroup( const F2FString groupname );

/** Finally friends (other peers) can be added to this group. This function triggers
 * the registration to ask the specified peer to join a F2F Computing group.
 * This means f2fFillIMSendBuffer
 * If we know his public key, we can send it as a challenge. He would then also get our publickey,
 * which could be compared to a allready cached one, to create an own challenge, and later used to
 * do encrypted and authenticated communication. Of course our own peerid from f2fInit is also
 * included.
 * - localPeerId will be the id used to send an IM message to this friend, it has to be managed
 * in the middle layer
 * - identifier can be the name in the addressbook or one of the addresses including the protocol,
 * example: "test@jabber.xyz (XMPP)"
 * This function will call the SendMethodIP-function*/
F2FError f2fGroupRegisterPeer( /*out*/ F2FGroup *group, const F2FWord32 localPeerId,
		const F2FString identifier, const F2FString inviteMessage,
		const F2FString otherPeersPublicKey );

/** unregister the peer again, must be in group 
 * changes group and peer (because of the embedded lists) */
F2FError f2fGroupUnregisterPeer( F2FGroup *group, F2FPeer *peer );

/** Return size of a peerlist in a group */
F2FSize f2fGroupGetPeerListSize( const F2FGroup *group );

/** Return a pointer to a peer of a group */
F2FPeer * f2fGroupGetPeerFromList( const F2FGroup *group, 
		F2FWord32 peerindex );

/** Getter for GroupUID */
F2FWord32 f2fGroupGetUIDHi(const F2FGroup * group);

/** Getter for GroupUID */
F2FWord32 f2fGroupGetUIDLo(const F2FGroup * group);

/* Send a text message to all group members */
F2FError f2fGroupSendText( F2FGroup *group, const F2FString message );

/** Send data to a peer in this group */
/* TODO: think if this is obsolet
 * F2FError f2fGroupPeerSendData( const F2FGroup *group, const F2FPeer *peer,
		const char *data, const F2FWord32 dataLen, int * datasentflag );*/

/** Return a random number from the seeded mersenne twister */
F2FWord32 f2fRandom();
/** Return a random number from the seeded mersenne twister, but not 0 */
F2FWord32 f2fRandomNotNull();

/* some getters for f2fpeers and f2fpeerlist*/
F2FWord32 f2fPeerGetUIDLo( const F2FPeer *peer );
F2FWord32 f2fPeerGetUIDHi( const F2FPeer *peer );
F2FWord32 f2fPeerGetLocalPeerId( const F2FPeer *peer );
/** Return size of the general peerlist */
F2FSize f2fPeerListGetSize();
/** Return a pointer to a peer in the global peerlist */
F2FPeer * f2fPeerListGetPeer( F2FWord32 peerindex );

/** Fill send buffer with data for all group members */
F2FError f2fGroupSendData( F2FGroup *group, 
		const char * message, F2FSize len );

/** Fill send buffer with a text message for all group members */
F2FError f2fGroupSendText( F2FGroup *group, const F2FString message );

/** Send data to specific peer in a group */
F2FError f2fPeerSendData( F2FGroup *group, F2FPeer *peer,
		F2FMessageType type,
		const char *data, const F2FWord32 dataLen );

/** Fill send buffer for a specific peer in a group with raw data */
F2FError f2fPeerSendRaw( F2FGroup *group, F2FPeer *peer, 
		const char *data, const F2FWord32 dataLen );

/** test, if data in buffer has been sent */
int f2fTestSentBufferEmpty();

/** empty, send buffer for data, even if it has not been sent */
F2FError f2fSetSentBufferEmpty();

/** submit a job to a f2f group
 * This will first ask for allowance tickets from 
 * every client in the job group. If at a later point
 * (when the job is already started) clients (more slaves)
 * are added to the group, they will be directly asked for a ticket.
 * If tickets are received back, the job will be sent to these clients.
 * The Job must be available as a local file in a special archive format
 */ 
F2FError f2fGroupSubmitJob( F2FGroup *group, const char * jobpath );

/** distribute file */
F2FError f2fGroupDistributeFile( const char * publishname,
		const char * filepath );

/** distribute data in distr. hash table */
F2FError f2fGroupDistributeData( const char * publishname,
		char * memorypool, F2FSize len );

#ifdef __cplusplus
    }
#endif

#endif /*F2FMAIN_H_*/
