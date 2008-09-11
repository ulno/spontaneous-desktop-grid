/***************************************************************************
 *   Filename: f2fconfig.h
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
 *   Description: all config settings for the f2fcore
 *   
 ***************************************************************************/

#ifndef F2FCONFIG_H_
#define F2FCONFIG_H_

/** Maximum length of name strings */
#define F2FMaxNameLength 255

/** Number of maximum known peers in one F2FCore */
#define F2FMaxPeers 1024

/** Number of groups a peer can join */
#define F2FMaxGroups 64

/** maximum message size, must be bigger than the maximum uuencoded different possible 
 * message types */
#define F2FMaxMessageSize 4096

#endif /*F2FCONFIG_H_*/
