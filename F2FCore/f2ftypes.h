/***************************************************************************
 *   Filename: f2ftypes.h
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
 *   Description: The types, which are uesd in F2FCore
 *   
 ***************************************************************************/

#ifndef F2FTYPES_H_
#define F2FTYPES_H_

#include <stdint.h> // We need to know what 32bit-word is, so we can still run on 64 and more

typedef int32_t F2FWord32;

typedef char *F2FString; // The string of F2F on C layer is just an array of characters terminated with 0

typedef long F2FSize; // a length of something (is signed as it could be negative in an error case)

/** Error return codes */
typedef enum  {
	F2FErrOK = 0, /** Successfull operation */
	F2FErrCreationFailed = -1, /** an object could not be created */
	F2FErrNotEnoughSeed = -2, /** Not enough input to create a seed */
	F2FErrInitNotCalled = -3, /** init must be called first */
	F2FErrListEmpty = -4, /** Trying to access an object from an empty list */
	F2FErrNotFound = -5, /** Could not find the object searched for */
	F2FErrListFull = -6, /** Not enough memory reserved to add this object */
	F2FErrNothingAvail = -6, /** Not enough memory reserved to add this object */
} F2FError;

/** the unique identifier used in F2F 
 * first this will be only 64 bits, which should be ok, if there are about max. 1024 clients in a frid */
typedef struct
{
	F2FWord32 hi;
	F2FWord32 lo;
} F2FUID;

/* static list with providers including their send and receive stuff*/ 
/** every peer has this info (used by all providers) */
typedef struct
{
	int activeprovider; /** the active provider. The one over which data is send and from which data is received */
	/** Local Ip number */
	/** Ip number of router */
	/** which providers are trying to connect, in which state are they ... */
} F2FPeerCommunicationProviderInfo;

/** A peer is represented by its random 64 bit id */
typedef struct
{
	F2FUID id;
	F2FPeerCommunicationProviderInfo communicationproviderinfo; /** Space for the comm. providers 
	 														   * includes active provider */
	F2FWord32 localPeerId; /** The id in the adapterlayer to reference how
	 * to send via the IM channel */ 
} F2FPeer;

#endif /*F2FTYPES_H_*/
